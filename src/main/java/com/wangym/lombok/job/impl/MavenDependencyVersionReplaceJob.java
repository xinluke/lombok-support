package com.wangym.lombok.job.impl;

import com.wangym.lombok.job.AbstractJob;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.jdom.MavenJDOMWriter;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wangym
 * @version 创建时间：2018年6月25日 下午1:40:20
 */
@ConditionalOnProperty(value = "maven.trim.enable", havingValue = "true")
@Component
@Slf4j
public class MavenDependencyVersionReplaceJob extends AbstractJob {

    @Autowired
    private DependencyVersionService dvService;

    public MavenDependencyVersionReplaceJob() {
        super("pom.xml");
    }

    @Override
    public void exec(File file) {
        try {
            doHandle(file);
        } catch (Exception e) {
            log.info("处理文件失败：{}", file.getPath(), e);
        }
    }

    private void doHandle(File file) throws FileNotFoundException, IOException, XmlPullParserException, JDOMException {
        // Reading
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileInputStream(file));
        // Editing
        ModelWrapper modelWrapper = new ModelWrapper(dvService, model);
        modelWrapper.process();
        if (modelWrapper.isHasModify()) {
            log.debug("文件需要更新:{}",file.getPath());
            // Writing
            new MavenJDOMWriter(model)
                    .setExpandEmptyElements(false)// pom.xml需要简化配置，所以override原本的配置，设置为自闭合
                    .write(model, file);
        }
    }

    @Getter
    class ModelWrapper {
        private DependencyVersionService dvService;
        private Model model;
        private boolean hasModify = false;
        // 白名单列表
        private List<String> standardList = Arrays.asList("java.version");

        public ModelWrapper(DependencyVersionService dvService, Model model) {
            super();
            this.dvService = dvService;
            this.model = model;
        }

        public void process() {
            List<Dependency> dep = model.getDependencies();
            for (Dependency d : dep) {
                processDependency(d);
            }
            Optional.ofNullable(model.getDependencyManagement())
                    .map(it->it.getDependencies())
                    .orElse(Collections.emptyList())
                    .forEach(this::processDependency);
            //因为${project.version}的声明，在读取的时候，会当做null的版本来处理，这样的依赖是不能去除的
//            List<Dependency> dep2 = getDependencyOfDependencyManagement();
//            for (Iterator<Dependency> iterator = dep2.iterator(); iterator.hasNext(); ) {
//                Dependency dependency = iterator.next();
//                //在依赖管理器中的版本不能为空，这样会导致依赖出错
//                if (dependency.getVersion() == null) {
//                    iterator.remove();
//                }
//            }

            // 删除重复的依赖声明
            deleteDuplicateDependencies(model.getDependencies());
            DependencyManagement dm = model.getDependencyManagement();
            if (dm != null) {
                deleteDuplicateDependencies(dm.getDependencies());
            }
            mergeProperty();
        }

        private List<Dependency> getDependencyOfDependencyManagement() {
            return Optional.ofNullable(model.getDependencyManagement())
                    .map(it -> it.getDependencies())
                    .orElse(Collections.emptyList());
        }
        private void processDependency(Dependency d){
            String a = d.getArtifactId();
            String version = d.getVersion();
            if(dvService.getHiddenVersionArtifactIdList().contains(a)) {
                // 去除版本号相关声明
                d.setVersion(null);
                notifyHasModify();
                return;
            }

            if (isNormalRefPropertyVersion(d)) {
                String versionValue = getRefVersionKey(d);
                //这种情况是不标准的，应该把版本号的声明放在父类中，有可能是从父类pom.xml复制过来忘记删除掉版本号声明了
                if (!hasVersionProperty(versionValue)) {
                    d.setVersion(null);
                    notifyHasModify();
                }
            } else if (StringUtils.isNotEmpty(version)) {
                //这种情况，说明是引用了属性的变量，但是不是很规范
                //需要统一规范一下
                String realVer;
                if (version.contains("$")) {
                    realVer = getRefVersionValue(d);
                    //因为是不标准的版本属性声明，需要显式指定进行删除
                    model.getProperties().remove(getRefVersionKey(version));
                } else {
                    realVer = version;
                }
                // 如果是原始的值类型的版本号才进行处理
                insertProperty(a, realVer);
                d.setVersion(getNewVersion(a));
                notifyHasModify();
            }
        }

        private void notifyHasModify() {
            // 说明pom.xml有变动
            hasModify = true;
        }

        private boolean hasVersionProperty(String versionKey) {
            Properties prop = model.getProperties();
            List<String> refVersionList = prop.stringPropertyNames().stream() //
                    .filter((item) -> {
                        return !standardList.contains(item) && item.endsWith(".version");
                    }) // 只关心我们自定义的version属性
                    .collect(Collectors.toList());
            return refVersionList.contains(versionKey);
        }

        private void mergeProperty() {
            // 得到最全的版本列表
            List<String> versionList = getVersionList(model);
            // 将重复的属性声明合并，在maven的默认处理中，同名的多次声明只有最后一次是生效的
            Properties prop = model.getProperties();
            List<String> refVersionList = prop.stringPropertyNames().stream() //
                    .filter((item) -> {
                        return !standardList.contains(item) && item.endsWith(".version");
                    }) // 只关心我们自定义的version属性
                    .collect(Collectors.toList());
            // 得到无用的依赖版本列表
            refVersionList.removeAll(versionList);
            if(!refVersionList.isEmpty()) {
                // 去除无用的依赖版本列表
                refVersionList.forEach(prop::remove);
                // 说明pom.xml有变动
                hasModify = true;
            }

            // 如果存在依赖的版本号，则更新版本号
            checkAndUpdateVersion();
            checkAndUpdateParentVersion();
            if (dvService.isDeleteDistributionManagement() && model.getDistributionManagement() != null) {
                model.setDistributionManagement(null);
                // 说明pom.xml有变动
                hasModify = true;
            }
            if (dvService.isDeleteDependencyManagement() && model.getDependencyManagement() != null) {
                model.setDependencyManagement(null);
                // 说明pom.xml有变动
                hasModify = true;
            }
            // 新增依赖
            List<DepdencyModel> insertList = dvService.getInsertList();
            if (insertList != null) {
                insertList.forEach(it -> {
                    // 不存在再添加
                    if(emptyDepdency(it.getGroupId(),it.getArtifactId())) {
                        model.getDependencies().add(it.build());
                    }
                });
            }
        }
        private boolean emptyDepdency(String groupId,String artifactId){
            List<Dependency> dependencies = model.getDependencies();
            long count = dependencies
                    .stream()
                    .filter(it -> groupId.equals(it.getGroupId()) && artifactId.equals(it.getArtifactId()))
                    .count();
            return count==0;
        }

        private void checkAndUpdateVersion() {
            Properties prop = model.getProperties();
            for (VerModelData item : dvService.getUpdateList()) {
                if (prop.containsKey(item.getName())) {
                    prop.setProperty(item.getName(), item.getVersion());
                    // 说明pom.xml有变动
                    hasModify = true;
                }
            }
            // 按要求删除依赖
            List<Dependency> dependencies = model.getDependencies();
            for (Iterator<Dependency> iterator = dependencies.iterator(); iterator.hasNext();) {
                Dependency dependency = iterator.next();
                for (VerModelData item : dvService.getDeleteList()) {
                    if (dependency.getArtifactId().equals(item.getName())) {
                        iterator.remove();
                        // 说明pom.xml有变动
                        hasModify = true;
                        break;
                    }
                }
                for (ReplaceModel item : dvService.getReplaceList()) {
                    //替换成目标依赖
                    if (dependency.getArtifactId().equals(item.getSource().getArtifactId())) {
                        DepdencyModel target = item.getTarget();
                        dependency.setArtifactId(target.getArtifactId());
                        dependency.setGroupId(target.getGroupId());
                        dependency.setVersion(target.getVersion());
                        // 说明pom.xml有变动
                        hasModify = true;
                        break;
                    }
                }
            }
        }

        private void checkAndUpdateParentVersion() {
            Parent p = model.getParent();
            if (p == null) {
                return;
            }
            for (VerModelData item : dvService.getParentList()) {
                if (p.getArtifactId().equals(item.getName())) {
                    p.setVersion(item.getVersion());
                    // 说明pom.xml有变动
                    hasModify = true;
                }
            }
        }

        private void deleteDuplicateDependencies(List<Dependency> dependencies) {
            // 删除重复的依赖声明
            Map<String, Long> collect = dependencies
                    .stream()
                    .map(Dependency::toString)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            collect.forEach((k, v) -> {
                //如果不唯一则操作
                if (v > 1) {
                    checkDependenciesByKey(dependencies, k, v);
                }
            });
        }

        private void checkDependenciesByKey(List<Dependency> dependencies, String key, long index) {
            for (Iterator<Dependency> iterator = dependencies.iterator(); iterator.hasNext(); ) {
                Dependency dependency = iterator.next();
                if (key.equals(dependency.toString()) && index > 1) {
                    iterator.remove();
                    // 说明pom.xml有变动
                    hasModify = true;
                    index--;
                }
            }
        }

        private void insertProperty(String artifactId, String version) {
            String key = artifactId + ".version";
            Properties prop = model.getProperties();
            // 如果当前key存在会产生覆盖效果
            prop.setProperty(key, version);
        }

        private String getNewVersion(String artifactId) {
            String key = artifactId + ".version";
            return "${" + key + "}";
        }

        private boolean isRefPropertyVersion(String version) {
            if (version != null && version.startsWith("${") && version.endsWith("}")) {
                return true;
            }
            return false;
        }

        /**
         * 判断是否是标准的属性引用
         *
         * @param dependency
         * @return
         */
        private boolean isNormalRefPropertyVersion(Dependency dependency) {
            String version = dependency.getVersion();
            if (version != null && version.startsWith("${") && version.endsWith(".version}")) {
                return true;
            }
            return false;
        }
        private List<String> getVersionList(Model model) {
            List<Dependency> dep1 = model.getDependencies();
            List<Dependency> dep2 = Optional.ofNullable(model.getDependencyManagement())
                    .map(it->it.getDependencies())
                    .orElse(Collections.emptyList());
            ArrayList<Dependency> list = new ArrayList<>();
            list.addAll(dep1);
            list.addAll(dep2);
            return getVersionList(list);
        }

        private String getRefVersionKey(Dependency dependency) {
            String version = dependency.getVersion();
            return getRefVersionKey(version);
        }

        private String getRefVersionKey(String version) {
            if(isRefPropertyVersion(version)) {
                int length = version.length();
                //"${" + key + "}",去除包裹的的模式字符
                return version.substring(2, length - 1);
            }
            return null;
        }

        private String getRefVersionValue(Dependency dependency) {
            Properties prop = model.getProperties();
            return prop.getProperty(getRefVersionKey(dependency));
        }

        private List<String> getVersionList(List<Dependency> dependencies) {
            List<String> versionList = dependencies.stream()
                    .filter(this::isNormalRefPropertyVersion)
                    .map(this::getRefVersionKey)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return versionList;
        }

    }


}
