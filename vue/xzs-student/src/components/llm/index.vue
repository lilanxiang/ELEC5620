<template>
  <div class="chat-component">
    <!-- 聊天图标 -->
    <div class="chat-icon" @click="toggleChat">
      <img src="../../assets/logo2.png" alt="Chat Icon" />
    </div>
    
    <!-- 聊天框 -->
    <div class="chat-box" v-if="isChatOpen">
      <div class="chat-header">
        <h3>聊天窗口</h3>
      </div>

      <div class="chat-content">
        <!-- 这里可以显示聊天的历史记录 -->
        <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
          <p>{{ msg.content }}</p>
        </div>
      </div>

      <div class="chat-input">
        <input v-model="message" placeholder="输入消息..." @keyup.enter="sendMessage" />
        <button @click="sendMessage">发送</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      isChatOpen: false,  // 控制聊天框显示
      message: '',        // 用户输入的消息
      messages: [],       // 保存消息记录
    };
  },
  methods: {
    // 切换聊天框的显示和隐藏
    toggleChat() {
      this.isChatOpen = !this.isChatOpen;
    },

    // 发送消息，并调用后端接口
    sendMessage() {
      if (this.message.trim()) {
        // 添加用户消息到聊天记录
        this.messages.push({ role: 'user', content: this.message });

        // 调用后端 API
        axios.post('/api/call-llm', {
          model: 'gpt-4',
          messages: [
            { role: 'system', content: 'You are a helpful assistant.' },
            ...this.messages  // 包含之前的所有聊天记录，形成对话历史
          ],
          max_tokens: 150,
          temperature: 0.7
        }, {
          headers: {
            'Content-Type': 'application/json'
          }
        })
        .then(response => {
          const answer = response.data.choices[0].message.content.trim();
          this.messages.push({ role: 'assistant', content: answer });  // 将响应内容添加到聊天记录中
        })
        .catch(error => {
          console.error('Error:', error);
          this.messages.push({ role: 'assistant', content: '出现错误，请稍后重试。' });
        });

        // 清空输入框
        this.message = '';
      }
    }
  }
};
</script>

<style scoped>
.chat-component {
  position: fixed;
  right: 20px;
  bottom: 20px;
}

.chat-icon {
  width: 50px;
  height: 50px;
  cursor: pointer;
}

.chat-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.chat-box {
  width: 350px;
  height: 400px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  position: fixed;
  right: 20px;
  bottom: 80px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.chat-header {
  background-color: #3b82f6;
  color: white;
  padding: 5px;
  display: flex;
  justify-content: center;
  border-radius: 10px 10px 0 0;
  font-size: 12px;
  height: 30px;
}

.chat-content {
  padding: 8px;
  flex-grow: 1;
  overflow-y: auto;
}

.message {
  margin-bottom: 12px;
  padding: 10px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  color: #333; /* 深色字体，增强对比度 */
}

.message.user {
  background-color: #e0f7fa; /* 用户消息的背景色，浅蓝色 */
  text-align: right; /* 用户消息靠右显示 */
}

.message.assistant {
  background-color: #f1f1f1; /* 助手消息的背景色，浅灰色 */
  text-align: left; /* 助手消息靠左显示 */
}

.chat-input {
  display: flex;
  padding: 6px;
  border-top: 1px solid #ccc;
  background-color: #f9f9f9;
  border-radius: 0 0 10px 10px;
}

.chat-input input {
  flex-grow: 3;
  padding: 6px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 12px;
}

.chat-input button {
  flex-grow: 0.5;
  margin-left: 6px;
  padding: 6px 12px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 12px;
}

.chat-input button:hover {
  background-color: #2563eb;
}
</style>
