<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { BORROW_PURPOSES, createBorrowRecord, type BorrowDirection, type BorrowRecord } from '@/services/borrowRecords'

const props = defineProps<{ open: boolean; direction: BorrowDirection; artifactName?: string }>()
const emit = defineEmits<{ 'update:open': [value: boolean]; saved: [record: BorrowRecord] }>()
const formRef = ref()
const submitting = ref(false)
const form = reactive({ artifactName: '', counterparty: '', purpose: undefined as string | undefined, startDate: '', endDate: '', remark: '' })

const resetForm = () => {
  form.artifactName = props.artifactName || ''
  form.counterparty = ''
  form.purpose = undefined
  form.startDate = ''
  form.endDate = ''
  form.remark = ''
  formRef.value?.clearValidate?.()
}

watch(() => props.open, (open) => { if (open) resetForm() })
watch(() => props.artifactName, (name) => { if (props.open) form.artifactName = name || '' })

const validateEndDate = async () => {
  if (form.startDate && form.endDate && form.endDate < form.startDate) throw new Error('结束/归还日期不能早于开始日期')
}
const rules = {
  artifactName: [{ required: true, message: '请输入藏品名称' }],
  counterparty: [{ required: true, message: props.direction === 'OUT' ? '请输入借入方名称' : '请输入来源机构' }],
  purpose: [{ required: true, message: '请选择借展用途' }],
  startDate: [{ required: true, message: '请选择开始日期' }],
  endDate: [{ required: true, message: props.direction === 'OUT' ? '请选择借展结束日期' : '请选择预计归还日期' }, { validator: validateEndDate }]
}

async function submit() {
  try {
    await formRef.value.validate()
    submitting.value = true
    const record = createBorrowRecord({
      direction: props.direction,
      artifactName: form.artifactName.trim(),
      counterparty: form.counterparty.trim(),
      purpose: form.purpose as (typeof BORROW_PURPOSES)[number],
      startDate: form.startDate,
      endDate: form.endDate,
      remark: form.remark.trim() || undefined
    })
    emit('saved', record)
    emit('update:open', false)
    message.success(`${props.direction === 'OUT' ? '借出' : '借入'}记录已保存到本地借展台账`)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <a-modal :open="open" :title="direction === 'OUT' ? '登记藏品借出' : '登记藏品借入'" :confirm-loading="submitting" ok-text="保存记录" cancel-text="取消" @update:open="emit('update:open', $event)" @ok="submit">
    <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
      <a-form-item label="藏品名称" name="artifactName"><a-input v-model:value="form.artifactName" :disabled="direction === 'OUT' && Boolean(artifactName)" :placeholder="direction === 'OUT' ? '' : '请输入借入藏品名称'" /></a-form-item>
      <a-form-item :label="direction === 'OUT' ? '借入方名称' : '来源机构'" name="counterparty"><a-input v-model:value="form.counterparty" :placeholder="direction === 'OUT' ? '如：国家博物馆' : '如：故宫博物院'" /></a-form-item>
      <a-form-item label="借展用途" name="purpose"><a-select v-model:value="form.purpose" placeholder="请选择借展用途"><a-select-option v-for="purpose in BORROW_PURPOSES" :key="purpose" :value="purpose">{{ purpose }}</a-select-option></a-select></a-form-item>
      <a-row :gutter="12"><a-col :span="12"><a-form-item :label="direction === 'OUT' ? '借展开始日期' : '借入开始日期'" name="startDate"><a-date-picker v-model:value="form.startDate" value-format="YYYY-MM-DD" style="width: 100%" /></a-form-item></a-col><a-col :span="12"><a-form-item :label="direction === 'OUT' ? '借展结束日期' : '预计归还日期'" name="endDate"><a-date-picker v-model:value="form.endDate" value-format="YYYY-MM-DD" style="width: 100%" /></a-form-item></a-col></a-row>
      <a-form-item label="备注"><a-textarea v-model:value="form.remark" :rows="3" :maxlength="300" show-count placeholder="可选" /></a-form-item>
    </a-form>
  </a-modal>
</template>
