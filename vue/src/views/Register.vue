<template>
  <div class="register-container">
    <!-- 装饰性背景元素 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="circle circle-4"></div>
    </div>

    <!-- 注册卡片 -->
    <div class="register-card">
      <div class="card-header">
        <div class="logo-section">
          <div class="logo-icon">
            <i class="el-icon-user-solid"></i>
          </div>
          <h1 class="title">创建账号</h1>
          <p class="subtitle">加入知识图谱系统，开启智能旅程</p>
        </div>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" class="register-form">
        <div class="form-row">
          <el-form-item prop="name" class="form-col">
            <div class="input-wrapper">
              <i class="el-icon-user input-icon"></i>
              <el-input 
                placeholder="姓名" 
                v-model="form.name"
                class="custom-input"
              ></el-input>
            </div>
          </el-form-item>

          <el-form-item prop="phone" class="form-col">
            <div class="input-wrapper">
              <i class="el-icon-phone input-icon"></i>
              <el-input 
                placeholder="手机号" 
                v-model="form.phone"
                class="custom-input"
              ></el-input>
            </div>
          </el-form-item>
        </div>

        <el-form-item prop="email">
          <div class="input-wrapper">
            <i class="el-icon-message input-icon"></i>
            <el-input 
              placeholder="请输入邮箱" 
              v-model="form.email"
              class="custom-input"
            ></el-input>
          </div>
        </el-form-item>

        <el-form-item prop="username">
          <div class="input-wrapper">
            <i class="el-icon-user-solid input-icon"></i>
            <el-input 
              placeholder="请输入用户名" 
              v-model="form.username"
              class="custom-input"
            ></el-input>
          </div>
        </el-form-item>
        
        <el-form-item prop="password">
          <div class="input-wrapper">
            <i class="el-icon-lock input-icon"></i>
            <el-input 
              placeholder="请输入密码" 
              show-password  
              v-model="form.password"
              class="custom-input"
            ></el-input>
          </div>
        </el-form-item>

        <el-form-item prop="confirmPass">
          <div class="input-wrapper">
            <i class="el-icon-lock input-icon"></i>
            <el-input 
              placeholder="请确认密码" 
              show-password  
              v-model="form.confirmPass"
              class="custom-input"
              @keyup.enter.native="register"
            ></el-input>
          </div>
        </el-form-item>

        <el-form-item class="register-btn-item">
          <el-button class="register-btn" @click="register">
            <span>立即注册</span>
            <i class="el-icon-check"></i>
          </el-button>
        </el-form-item>

        <div class="footer-links">
          <span class="login-hint">
            已有账号？<router-link to="/user/login" class="link">立即登录</router-link>
          </span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: "Register",
  data() {
    // 验证码校验
    const validatePassword = (rule, confirmPass, callback) => {
      if (confirmPass === '') {
        callback(new Error('请确认密码'))
      } else if (confirmPass !== this.form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      form: {},
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ],
        confirmPass: [
          { validator: validatePassword, trigger: 'blur' }
        ]
      }
    }
  },
  created() {

  },
  methods: {
    register() {
      this.$refs['formRef'].validate((valid) => {
        if (valid) {
          // 验证通过
          this.$request.post('/user/register', this.form).then(res => {
            if (res.code === '200') {
              this.$router.push('/login')  // 跳转登录页面
              this.$message.success('注册成功')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  overflow-x: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #FA8BFF 0%, #2BD2FF 25%, #2BFF88 50%, #FFA500 75%, #FF6B9D 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  position: relative;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 装饰性背景元素 */
.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 350px;
  height: 350px;
  top: -150px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 250px;
  height: 250px;
  bottom: -80px;
  right: 8%;
  animation-delay: 5s;
}

.circle-3 {
  width: 180px;
  height: 180px;
  top: 15%;
  right: -60px;
  animation-delay: 10s;
}

.circle-4 {
  width: 120px;
  height: 120px;
  top: 60%;
  left: 10%;
  animation-delay: 15s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(120deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(240deg);
  }
}

/* 注册卡片 */
.register-card {
  width: 500px;
  max-width: 95%;
  padding: 50px 45px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 
              0 0 0 1px rgba(255, 255, 255, 0.5);
  position: relative;
  z-index: 2;
  animation: slideUp 0.6s ease-out;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.register-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.2), 
              0 0 0 1px rgba(255, 255, 255, 0.5);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 卡片头部 */
.card-header {
  text-align: center;
  margin-bottom: 35px;
}

.logo-section {
  animation: fadeIn 0.8s ease-out 0.2s both;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logo-icon {
  width: 70px;
  height: 70px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #FA8BFF 0%, #2BD2FF 100%);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: white;
  box-shadow: 0 10px 25px rgba(43, 210, 255, 0.3);
  transition: transform 0.3s ease;
}

.logo-icon:hover {
  transform: scale(1.1) rotate(-5deg);
}

.title {
  font-size: 32px;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 10px 0;
  background: linear-gradient(135deg, #FA8BFF 0%, #2BD2FF 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 14px;
  color: #718096;
  margin: 0;
}

/* 表单样式 */
.register-form {
  animation: fadeIn 0.8s ease-out 0.4s both;
}

.register-form .el-form-item {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-col {
  flex: 1;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: #f7fafc;
  border-radius: 12px;
  padding: 0 18px;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.input-wrapper:hover {
  background: #edf2f7;
}

.input-wrapper:focus-within {
  background: white;
  border-color: #2BD2FF;
  box-shadow: 0 0 0 3px rgba(43, 210, 255, 0.1);
}

.input-icon {
  font-size: 18px;
  color: #a0aec0;
  margin-right: 12px;
  transition: color 0.3s ease;
}

.input-wrapper:focus-within .input-icon {
  color: #2BD2FF;
}

.custom-input {
  flex: 1;
}

.custom-input >>> .el-input__inner {
  border: none;
  background: transparent;
  padding: 14px 0;
  font-size: 15px;
  color: #2d3748;
  height: auto;
}

.custom-input >>> .el-input__inner::placeholder {
  color: #a0aec0;
}

.custom-input >>> .el-input__inner:focus {
  background: transparent;
}

.custom-input >>> .el-input__suffix {
  right: 0;
}

.custom-input >>> .el-icon-view {
  color: #a0aec0;
  transition: color 0.3s ease;
}

.custom-input >>> .el-icon-view:hover {
  color: #2BD2FF;
}

/* 注册按钮 */
.register-btn-item {
  margin-bottom: 20px;
  margin-top: 28px;
}

.register-btn {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #FA8BFF 0%, #2BD2FF 100%);
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 8px 20px rgba(43, 210, 255, 0.3);
  position: relative;
  overflow: hidden;
}

.register-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(43, 210, 255, 0.4);
}

.register-btn:hover::before {
  left: 100%;
}

.register-btn:active {
  transform: translateY(0);
  box-shadow: 0 5px 15px rgba(43, 210, 255, 0.3);
}

.register-btn i {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.register-btn:hover i {
  transform: scale(1.2);
}

/* 底部链接 */
.footer-links {
  text-align: center;
  margin-top: 20px;
  animation: fadeIn 0.8s ease-out 0.6s both;
}

.login-hint {
  font-size: 14px;
  color: #718096;
}

.link {
  color: #2BD2FF;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
}

.link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #FA8BFF, #2BD2FF);
  transition: width 0.3s ease;
}

.link:hover {
  color: #FA8BFF;
}

.link:hover::after {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
    padding: 20px 10px;
  }

  .register-card {
    width: 95%;
    padding: 35px 25px;
  }

  .title {
    font-size: 26px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .circle {
    display: none;
  }
}
</style>