<template>
  <div>
    <div class="search">
      <el-input placeholder="Search by title" style="width: 200px" v-model="title"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">Search</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">Reset</el-button>
    </div>

    <div class="operation">
<!--      <el-button type="primary" plain @click="handleAdd">Add</el-button>-->
      <el-button type="danger" plain @click="delBatch">Batch Delete</el-button>
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
  name: "ArticleInfo",
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
    handleAdd() {
      this.form = {}
      this.fromVisible = true
    },
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
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/articleInfo/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          title: this.title,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
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
.search, .operation, .table {
  margin-bottom: 20px;
}
.pagination {
  text-align: center;
}
</style>