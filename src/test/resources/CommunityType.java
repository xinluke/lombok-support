package com.wangym.community.iportal.bean;

public interface CommunityType {
    /**
     * 审核通过文章
     */
    public final static int PASS_ARTICLE = 335;

    /**
     * 审核不通过文章
     */
    public final static int REJECT_ARTICLE = 336;

    /**
     * 删除评论回复
     */
    public final static int REMOVE_COMMENT_REPLY = 330;

    /**
     * 屏蔽评论回复
     */
    public final static int HIDDEN_COMMENT_REPLY = 331;

    /**
     * 审核通过评论回复
     */
    public final static int PASS_COMMENT_REPLY = 332;

    /**
     * 审核不通过评论回复
     */
    public final static int REJECT_COMMENT_REPLY = 333;

    /**
     * 删除专栏
     */
    public final static int REMOVE_COLUMN = 323;

    /**
     * 屏蔽专栏
     */
    public final static int HIDDEN_COLUMN = 324;

    /**
     * 审核通过专栏
     */
    public final static int PASS_COLUMN = 325;

    /**
     * 审核不通过专栏
     */
    public final static int REJECT_COLUMN = 326;

    /**
     * 审核通过专栏
     */
    public final static int PASS_COMMENT = 327;

    /**
     * 审核不通过专栏
     */
    public final static int REJECT_COMMENT = 328;

    /**
     * 屏蔽文章
     */
    public final static int HIDDEN_ARTICLE = 308;

    /**
     * 推荐文章
     */
    public final static int RECOMMEND_ARTICLE = 309;

    /**
     * 删除答案回复
     */
    public final static int REMOVE_ANSWER_REPLY = 228;

    /**
     * 屏蔽答案回复
     */
    public final static int HIDDEN_ANSWER_REPLY = 229;

    /**
     * 审核通过答案回复
     */
    public final static int PASS_ANSWER_REPLY = 230;

    /**
     * 审核不通过答案回复
     */
    public final static int REJECT_ANSWER_REPLY = 231;

    /**
     * 审核通过问题
     */
    public final static int PASS_ASK = 224;

    /**
     * 审核不通过问题
     */
    public final static int REJECT_ASK = 225;

    /**
     * 审核通过问题
     */
    public final static int PASS_ANSWER = 226;

    /**
     * 审核不通过答案
     */
    public final static int REJECT_ANSWER = 227;

    /**
     * 屏蔽问题
     */
    public final static int HIDDEN_ASK = 210;

    /**
     * 屏蔽回答
     */
    public final static int HIDDEN_ANSWER = 211;

    /**
     * 屏蔽评论
     */
    public final static int HIDDEN_COMMENT = 212;

    /**
     * 推荐回答
     */
    public final static int RECOMMEND_ANSWER = 213;

    /**
     * 禁言
     */
    public final static int FORBID_SPEAKING = 105;

    /**
     * 屏蔽用户介绍
     */
    public final static int HIDDEN_INTRODUCE = 106;
}
