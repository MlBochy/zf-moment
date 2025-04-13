export const mockMoments = [
    {
      id: 1,
      content: "测试动态内容",
      images: [
        "https://dummyimage.com/300x200/4CAF50/fff",
        "https://dummyimage.com/300x200/2196F3/fff"
      ],
      user: {
        id: 1,
        nickname: "测试用户",
        avatar: "/default-avatar.jpg"
      },
      likeCount: 12,
      liked: false,
      comments: [
        { 
          user: { nickname: "好友A" },
          content: "测试评论内容"
        }
      ]
    }
  ]