<template>
  <div>
    <div class="header-row">
      <div class="search">
        <el-button type="danger" plain @click="delBatch">Batch Delete</el-button>
        <el-input placeholder="Search by title" style="width: 220px" v-model="title"></el-input>
        <el-button type="info" plain @click="load(1)">Search</el-button>
        <el-button type="warning" plain @click="reset">Reset</el-button>
      </div>
    </div>

    <div class="table">
      <el-table :data="tableData" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="ID" width="70" align="center" sortable></el-table-column>
        <el-table-column prop="title" label="Title"></el-table-column>
        <el-table-column prop="author" label="Author"></el-table-column>
        <el-table-column prop="srcDatabase" label="Source DB"></el-table-column>
        <el-table-column prop="year" label="Year"></el-table-column>
        <el-table-column prop="doi" label="DOI"></el-table-column>
        <el-table-column label="Actions" align="center" width="180">
          <template v-slot="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">Edit</el-button>
            <el-button size="mini" type="danger" plain @click="del(scope.row.id)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
            background
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[5, 10, 20]"
            :page-size="pageSize"
            layout="total, prev, pager, next"
            :total="total">
        </el-pagination>
      </div>
    </div>

    <el-dialog title="Article Information" :visible.sync="fromVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="form" label-width="100px" style="padding-right: 50px" :rules="rules" ref="formRef">
        <el-form-item label="Source DB" prop="srcDatabase">
          <el-input v-model="form.srcDatabase" placeholder="Source DB"></el-input>
        </el-form-item>
        <el-form-item label="Title" prop="title">
          <el-input v-model="form.title" placeholder="Title"></el-input>
        </el-form-item>
        <el-form-item label="Author" prop="author">
          <el-input v-model="form.author" placeholder="Author"></el-input>
        </el-form-item>
        <el-form-item label="Organization" prop="organ">
          <el-input v-model="form.organ" placeholder="Organization"></el-input>
        </el-form-item>
        <el-form-item label="Source" prop="source">
          <el-input v-model="form.source" placeholder="Source"></el-input>
        </el-form-item>
        <el-form-item label="Keywords" prop="keyword">
          <el-input v-model="form.keyword" placeholder="Keywords"></el-input>
        </el-form-item>
        <el-form-item label="Abstract" prop="summary">
          <el-input type="textarea" v-model="form.summary" placeholder="Abstract"></el-input>
        </el-form-item>
        <el-form-item label="Pub. Date" prop="pubTime">
          <el-input v-model="form.pubTime" placeholder="Pub. Date"></el-input>
        </el-form-item>
        <el-form-item label="Primary Author" prop="firstDuty">
          <el-input v-model="form.firstDuty" placeholder="Primary Author"></el-input>
        </el-form-item>
        <el-form-item label="Fund" prop="fund">
          <el-input v-model="form.fund" placeholder="Fund"></el-input>
        </el-form-item>
        <el-form-item label="Year" prop="year">
          <el-input v-model="form.year" placeholder="Year"></el-input>
        </el-form-item>
        <el-form-item label="Pages" prop="pageCount">
          <el-input v-model="form.pageCount" placeholder="Pages"></el-input>
        </el-form-item>
        <el-form-item label="CLC No." prop="clc">
          <el-input v-model="form.clc" placeholder="CLC No."></el-input>
        </el-form-item>
        <el-form-item label="URL" prop="url">
          <el-input v-model="form.url" placeholder="URL"></el-input>
        </el-form-item>
        <el-form-item label="DOI" prop="doi">
          <el-input v-model="form.doi" placeholder="DOI"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save">OK</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>

export default {
  name: "Articleedit",
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      title: null,
      fromVisible: false,
      form: {},
      rules: {
        title: [
          { required: true, message: 'Please enter title', trigger: 'blur' },
        ],
      },
      ids: []
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.fromVisible = true
    },
    save() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request({
            url: this.form.id ? '/articleInfo/update' : '/articleInfo/add',
            method: this.form.id ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {
              this.$message.success('Saved successfully')
              this.load(1)
              this.fromVisible = false
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    },
    del(id) {
      this.$confirm('Are you sure you want to delete?', 'Confirm Delete', { type: "warning" }).then(() => {
        this.$request.delete('/articleInfo/delete/' + id).then(res => {
          if (res.code === '200') {
            this.$message.success('Operation successful')
            this.load(1)
          } else {
            this.$message.error(res.msg)
          }
        })
      }).catch(() => {})
    },
    handleSelectionChange(rows) {
      this.ids = rows.map(v => v.id)
    },
    delBatch() {
      if (!this.ids.length) {
        this.$message.warning('Please select data')
        return
      }
      this.$confirm('Are you sure you want to delete selected items?', 'Confirm Delete', { type: "warning" }).then(() => {
        this.$request.delete('/articleInfo/delete/batch', { data: this.ids }).then(res => {
          if (res.code === '200') {
            this.$message.success('Operation successful')
            this.load(1)
          } else {
            this.$message.error(res.msg)
          }
        })
      }).catch(() => {})
    },
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum;
      const user = JSON.parse(localStorage.getItem('xm-user') || '{}');
      console.log('Current user info:', user); // Debug: check if xm-user exists
      console.log('Current token:', user.token); // Debug: check if token exists
      this.$request.get('/articleInfo/selectByUserId', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          title: this.title,
        }
      }).then(res => {
        console.log('Backend response data:', res); // Debug: check backend response
        this.tableData = res.data?.list || [];
        this.total = res.data?.total || 0;
      }).catch(err => {
        console.error('Request failed:', err); // Debug: catch request errors
        this.$message.error('Failed to load data, please check login status');
      });
    },
    reset() {
      this.title = null
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
  }
}
</script>

<style scoped>
/* Overall container style, smaller margins */
div {
  padding: 15px; /* Overall padding, slightly reduced */
  max-width: 1200px; /* Max width, keep content centered */
  margin: 0 auto; /* Horizontally centered */
}

/* Header row style, search and batch delete in one row */
.header-row {
  display: flex; /* Use flex layout */
  align-items: center; /* Vertically centered */
  justify-content: space-between; /* Space between: batch delete left, search right */
  margin-bottom: 20px; /* Spacing from table below */
}

/* Operation area style (batch delete) */
.operation {
  padding: 0; /* Remove padding, keep compact */
}

/* Search area style - beautified */
.search {
  display: flex; /* Flex layout for input and buttons */
  align-items: center; /* Vertically centered */
  padding: 10px 15px; /* Padding: 10px top/bottom, 15px left/right */
  background-color: #ffffff; /* White background */
  border-radius: 20px; /* Larger border radius, softer appearance */
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08); /* Softer shadow */
  border: 1px solid #dcdfe6; /* Element UI default border color */
  transition: all 0.3s; /* Transition effect */
}

/* Search box hover effect */
.search:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); /* Deeper shadow on hover */
}

/* Input style */
.search .el-input {
  width: 220px; /* Input width */
  margin-right: 10px; /* Spacing from button */
}

.search .el-input__inner {
  border-radius: 16px; /* Input border radius */
  border: 1px solid #dcdfe6; /* Border color */
}

/* Search area button style */
.search .el-button {
  margin-left: 10px; /* Spacing between buttons */
  border-radius: 16px; /* Rounded buttons */
}

/* Table area style */
.table {
  margin-bottom: 20px; /* Spacing from pagination below */
  padding: 12px; /* Padding */
  background-color: #ffffff; /* White background */
  border-radius: 6px; /* Border radius */
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); /* Slight shadow */
}

.el-table {
  border-radius: 6px; /* Table border radius */
  overflow: hidden; /* Prevent content overflow */
}

.el-table th {
  background-color: #fafafa; /* Table header background */
  font-weight: 600; /* Bold header font */
  color: #303133; /* Element UI default dark text */
}

.el-table td {
  color: #606266; /* Element UI default text color */
}

/* Pagination style */
.pagination {
  text-align: center; /* Center aligned */
  margin-top: 15px; /* Spacing from table above */
  padding: 8px 0; /* Padding */
}

/* Dialog style */
.el-dialog {
  border-radius: 8px; /* Border radius */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); /* Shadow */
}

.el-dialog__header {
  background-color: #409eff; /* Element UI primary color */
  color: #fff; /* White text */
  border-top-left-radius: 8px; /* Top-left border radius */
  border-top-right-radius: 8px; /* Top-right border radius */
  padding: 12px 20px; /* Padding */
}

.el-dialog__title {
  font-size: 16px; /* Title font size */
  font-weight: 500; /* Font weight */
}

.el-form {
  padding: 15px; /* Form padding */
}

.el-form-item {
  margin-bottom: 18px; /* Form item spacing */
}

.el-form-item__label {
  font-weight: 500; /* Bold label font */
  color: #303133; /* Default dark text */
}

.dialog-footer {
  text-align: right; /* Buttons aligned right */
  padding: 12px 20px; /* Padding */
  border-top: 1px solid #dcdfe6; /* Divider line */
}

/* Button style */
.el-button {
  border-radius: 4px; /* Default border radius */
  transition: all 0.3s; /* Transition effect */
}

.el-button:hover {
  opacity: 0.9; /* Opacity on hover */
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1); /* Slight shadow */
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .header-row {
    flex-direction: column; /* Vertical layout on small screens */
    align-items: flex-start; /* Left aligned */
  }

  .search {
    width: 100%; /* Search box full width */
    justify-content: center; /* Centered on small screens */
    margin-top: 10px; /* Spacing from batch delete */
  }

  .search .el-input,
  .search .el-button {
    width: 100%; /* Full width */
    margin: 8px 0; /* Vertical spacing */
  }

  .el-dialog {
    width: 90%; /* Dialog width adjustment */
  }
}
</style>