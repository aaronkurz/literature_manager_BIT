<template>
  <div class="container">
    <div class="register-box">
      <div class="title-wrapper">
        <div class="main-title">User Registration</div>
        <div class="sub-title">Start your digital journey</div>
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

        <el-form-item prop="confirmPass">
          <el-input
              prefix-icon="el-icon-lock"
              placeholder="Confirm password"
              show-password
              v-model="form.confirmPass"
              class="custom-input"
              @focus="handleInputFocus"
              @blur="handleInputBlur">
          </el-input>
        </el-form-item>

        <el-form-item prop="name">
          <el-input
              prefix-icon="el-icon-user"
              placeholder="Enter name"
              v-model="form.name"
              class="custom-input"
              @focus="handleInputFocus"
              @blur="handleInputBlur">
          </el-input>
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
              prefix-icon="el-icon-phone"
              placeholder="Enter phone number"
              v-model="form.phone"
              class="custom-input"
              @focus="handleInputFocus"
              @blur="handleInputBlur">
          </el-input>
        </el-form-item>

        <el-form-item prop="email">
          <el-input
              prefix-icon="el-icon-message"
              placeholder="Enter email"
              v-model="form.email"
              class="custom-input"
              @focus="handleInputFocus"
              @blur="handleInputBlur">
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
              class="register-btn"
              @mouseenter="handleBtnHover"
              @mouseleave="handleBtnLeave"
              @click="register">
            Register
          </el-button>
        </el-form-item>

        <div class="footer">
          <span>Already have an account?</span>
          <a href="/login" class="login-link">Log in now</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: "Register",
  data() {
    const validatePassword = (rule, confirmPass, callback) => {
      if (!confirmPass) {
        callback(new Error('Please confirm password'))
      } else if (confirmPass !== this.form.password) {
        callback(new Error('Passwords do not match'))
      } else {
        callback()
      }
    }

    return {
      form: {
        username: '',
        password: '',
        confirmPass: '',
        name: '',
        phone: '',
        email: ''
      },
      rules: {
        username: [
          { required: true, message: 'Please enter username', trigger: 'blur' },
          { min: 3, max: 20, message: 'Must be 3-20 characters', trigger: 'blur' }
        ],
        password: [
          { required: true, message: 'Please enter password', trigger: 'blur' },
          { min: 6, max: 20, message: 'Must be 6-20 characters', trigger: 'blur' }
        ],
        confirmPass: [
          { validator: validatePassword, trigger: 'blur' }
        ],
        name: [
          { required: true, message: 'Please enter name', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: 'Please enter a valid phone number', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    register() {
      const token = localStorage.getItem('token')
      const user = localStorage.getItem('xm-user')
      if (token || user) {
        this.$message.warning('Please log out before registering')
        return
      }

      this.$refs.formRef.validate(valid => {
        if (valid) {
          const params = {...this.form}
          delete params.confirmPass
          params.role = 'USER'

          this.$request.post('/api/user/register', params).then(res => {
            if (res.code === '200') {
              localStorage.removeItem('token')
              localStorage.removeItem('xm-user')
              this.$message.success('Registration successful')
              this.$router.push('./login')
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

.register-box {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 12px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.register-box:hover {
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

.register-btn {
  width: 100%;
  height: 44px;
  background: linear-gradient(135deg, #333, #444);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.register-btn:hover {
  background: linear-gradient(135deg, #444, #555);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.register-btn:active {
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

.login-link {
  color: #333;
  font-weight: 500;
  margin-left: 6px;
  text-decoration: none;
  transition: color 0.2s;
}

.login-link:hover {
  color: #409EFF;
  text-decoration: underline;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-form-item__content) {
  line-height: normal;
}

:deep(.el-form-item__error) {
  padding-top: 4px;
  font-size: 12px;
}
</style>