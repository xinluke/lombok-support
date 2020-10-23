@RequestMapping("/sdkBlackList")
public interface SdkBlackListServiceClient {

    @PostMapping("/count")
    int countSdkBlackList(@RequestBody SdkBlackListParam param);

    @PostMapping("/list")
    List<SdkBlackList> listSdkBlackList(@RequestBody SdkBlackListParam param);

    @PostMapping("/delete/{id}")
    int deleteSdkBlackList(@PathVariable("id") Integer id);

    @PostMapping("/add")
    int addSdkBlackList(@RequestBody SdkBlackList sdkBlackList);

    @GetMapping("/detail/{id}")
    SdkBlackList getSdkBlackList(@PathVariable("id") Integer id);

    @GetMapping("/selectName")
    List<String> selectSdkBlackListName();
}