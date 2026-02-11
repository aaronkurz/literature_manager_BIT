<template>
  <div class="container">
    <div class="login-box">
      <div class="title-wrapper">
        <div class="main-title">Academic Literature Management & Analysis System</div>
        <div class="sub-title">Please log in to your account</div>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input
              prefix-icon="el-icon-user"
              placeholder="Enter username"
              v-model="form.username"
              class="custom-input"
              @focus="handleInputFocus"
              @blur="handleInputBlur">
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
              prefix-icon="el-icon-lock"
              placeholder="Enter password"
              show-password
              v-model="form.password"
              class="custom-input"
              @focus="handleInputFocus"
              @blur="handleInputBlur">
          </el-input>
        </el-form-item>
        <el-form-item prop="role">
          <el-select
              v-model="form.role"
              placeholder="Select role"
              style="width: 100%"
              class="custom-select">
            <el-option label="Admin" value="ADMIN"></el-option>
            <el-option label="User" value="USER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
              class="login-btn"
              @mouseenter="handleBtnHover"
              @mouseleave="handleBtnLeave"
              @click="login">
            Log In
          </el-button>
        </el-form-item>
        <div class="footer">
          <span>Don't have an account?</span>
          <a href="/register" class="register-link">Register now</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      form: { role: 'USER' },
      rules: {
        username: [
          { required: true, message: 'Please enter username', trigger: 'blur' },
        ],
        password: [
          { required: true, message: 'Please enter password', trigger: 'blur' },
        ]
      }
    }
  },
  methods: {
    login() {
      this.$refs['formRef'].validate((valid) => {
        if (valid) {
          this.$request.post('/login', this.form).then(res => {
            if (res.code === '200') {
              localStorage.setItem("xm-user", JSON.stringify(res.data))
              if (res.data.role === 'ADMIN') {
                this.$router.push('/manager')
              } else {
                this.$router.push('/front/graph')
              }
              this.$message.success('Login successful')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    },
    handleInputFocus(e) {
      e.target.parentElement.parentElement.style.borderColor = '#409EFF'
    },
    handleInputBlur(e) {
      e.target.parentElement.parentElement.style.borderColor = '#DCDFE6'
    },
    handleBtnHover(e) {
      e.target.style.transform = 'translateY(-2px)'
      e.target.style.boxShadow = '0 5px 15px rgba(0,0,0,0.2)'
    },
    handleBtnLeave(e) {
      e.target.style.transform = 'translateY(0)'
      e.target.style.boxShadow = '0 2px 6px rgba(0,0,0,0.1)'
    }
  }
}
</script>

<style scoped>
.container {
  height: 100vh;
  overflow: hidden;
  background-image: url("@/assets/imgs/bg1.png");
  background-size: cover;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  transform: translateY(0);
  transition: all 0.3s ease;
}

.login-box:hover {
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}

.title-wrapper {
  text-align: center;
  margin-bottom: 30px;
}

.main-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.sub-title {
  font-size: 14px;
  color: #909399;
}

.custom-input :deep(.el-input__inner) {
  height: 44px;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.custom-input :deep(.el-input__prefix) {
  display: flex;
  align-items: center;
  left: 12px;
}

.custom-select :deep(.el-input__inner) {
  height: 44px;
  border-radius: 8px;
}

.login-btn {
  width: 100%;
  height: 44px;
  background: linear-gradient(135deg, #333333, #444444);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.login-btn:hover {
  background: linear-gradient(135deg, #444444, #555555);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  font-size: 14px;
  color: #909399;
}

.register-link {
  color: #333;
  font-weight: 500;
  margin-left: 6px;
  text-decoration: none;
  transition: color 0.2s;
}

.register-link:hover {
  color: #409EFF;
  text-decoration: underline;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-form-item__content) {
  line-height: normal;
}
</style>