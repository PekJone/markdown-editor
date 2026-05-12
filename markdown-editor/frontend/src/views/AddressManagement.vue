<template>
  <div class="address-management">
    <div class="page-header">
      <h2>地址管理</h2>
      <button class="add-btn" @click="showAddModal = true">+ 新增收货地址</button>
    </div>

    <div class="address-list" v-if="addresses.length > 0">
      <div 
        v-for="address in addresses" 
        :key="address.id" 
        class="address-card"
        :class="{ 'default': address.isDefault === 1 }"
      >
        <div class="address-header">
          <div class="user-info">
            <span class="name">{{ address.name }}</span>
            <span class="phone">{{ address.phone }}</span>
          </div>
          <span v-if="address.isDefault === 1" class="default-tag">默认地址</span>
        </div>
        <div class="address-detail">
          {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
        </div>
        <div class="address-actions">
          <button v-if="address.isDefault !== 1" @click="setDefault(address.id)" class="action-btn default-btn">设为默认</button>
          <button @click="editAddress(address)" class="action-btn edit-btn">编辑</button>
          <button @click="deleteAddress(address.id)" class="action-btn delete-btn">删除</button>
        </div>
      </div>
    </div>

    <div class="empty-state" v-else>
      <div class="empty-icon">📦</div>
      <p>暂无收货地址</p>
      <button @click="showAddModal = true">添加第一个地址</button>
    </div>

    <div class="modal-overlay" v-if="showAddModal || showEditModal" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑地址' : '新增地址' }}</h3>
          <span class="close-btn" @click="closeModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitAddress">
            <div class="form-group">
              <label>收货人</label>
              <input 
                v-model="formData.name" 
                type="text" 
                placeholder="请输入收货人姓名"
                required
              />
            </div>
            <div class="form-group">
              <label>联系电话</label>
              <input 
                v-model="formData.phone" 
                type="tel" 
                placeholder="请输入联系电话"
                required
              />
            </div>
            <div class="form-group">
              <label>省份</label>
              <input 
                v-model="formData.province" 
                type="text" 
                placeholder="请输入省份"
              />
            </div>
            <div class="form-group">
              <label>城市</label>
              <input 
                v-model="formData.city" 
                type="text" 
                placeholder="请输入城市"
              />
            </div>
            <div class="form-group">
              <label>区县</label>
              <input 
                v-model="formData.district" 
                type="text" 
                placeholder="请输入区县"
              />
            </div>
            <div class="form-group">
              <label>详细地址</label>
              <textarea 
                v-model="formData.detail" 
                placeholder="请输入详细地址"
              ></textarea>
            </div>
            <div class="form-group checkbox-group">
              <input 
                v-model="formData.isDefault" 
                type="checkbox" 
                id="isDefault"
              />
              <label for="isDefault">设为默认地址</label>
            </div>
            <div class="form-actions">
              <button type="button" class="cancel-btn" @click="closeModal">取消</button>
              <button type="submit" class="submit-btn">{{ isEditing ? '保存修改' : '添加地址' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>import { ref, onMounted } from 'vue';
import api from '@/utils/api';
const addresses = ref([]);
const showAddModal = ref(false);
const showEditModal = ref(false);
const isEditing = ref(false);
const formData = ref({
 id: null,
 name: '',
 phone: '',
 province: '',
 city: '',
 district: '',
 detail: '',
 isDefault: false
});
const loadAddresses = async () => {
 try {
 const response = await api.get('/api/addresses');
 addresses.value = response.data || [];
 }
 catch (error) {
 console.error('加载地址列表失败:', error);
 }
};
const openAddModal = () => {
 isEditing.value = false;
 formData.value = {
 id: null,
 name: '',
 phone: '',
 province: '',
 city: '',
 district: '',
 detail: '',
 isDefault: false
 };
 showAddModal.value = true;
};
const editAddress = (address) => {
 isEditing.value = true;
 formData.value = {
 id: address.id,
 name: address.name,
 phone: address.phone,
 province: address.province || '',
 city: address.city || '',
 district: address.district || '',
 detail: address.detail || '',
 isDefault: address.isDefault === 1
 };
 showEditModal.value = true;
};
const closeModal = () => {
 showAddModal.value = false;
 showEditModal.value = false;
};
const submitAddress = async () => {
 try {
 const addressData = {
 ...formData.value,
 isDefault: formData.value.isDefault ? 1 : 0
 };
 if (isEditing.value) {
 await api.put(`/api/addresses/${formData.value.id}`, addressData);
 }
 else {
 await api.post('/api/addresses', addressData);
 }
 closeModal();
 await loadAddresses();
 }
 catch (error) {
 console.error('保存地址失败:', error);
 alert('保存地址失败，请重试');
 }
};
const setDefault = async (addressId) => {
 try {
 await api.post(`/api/addresses/${addressId}/default`);
 await loadAddresses();
 }
 catch (error) {
 console.error('设置默认地址失败:', error);
 alert('设置默认地址失败，请重试');
 }
};
const deleteAddress = async (addressId) => {
 if (!confirm('确定要删除这个地址吗？'))
 return;
 try {
 await api.delete(`/api/addresses/${addressId}`);
 await loadAddresses();
 }
 catch (error) {
 console.error('删除地址失败:', error);
 alert('删除地址失败，请重试');
 }
};
onMounted(() => {
 loadAddresses();
});
</script>

<style scoped>
.address-management {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
}

.add-btn {
  padding: 8px 16px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.add-btn:hover {
  background: #67b8ff;
}

.address-list {
  display: grid;
  gap: 16px;
}

.address-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: white;
  transition: all 0.3s;
}

.address-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.address-card.default {
  border-color: #409eff;
  background: #f0f7ff;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.user-info {
  display: flex;
  gap: 16px;
}

.name {
  font-weight: bold;
  font-size: 16px;
}

.phone {
  color: #666;
}

.default-tag {
  background: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.address-detail {
  color: #666;
  margin-bottom: 12px;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 4px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.default-btn {
  background: #fff7e6;
  color: #e6a23c;
}

.default-btn:hover {
  background: #ffeeba;
}

.edit-btn {
  background: #e8f4fd;
  color: #409eff;
}

.edit-btn:hover {
  background: #d4eafd;
}

.delete-btn {
  background: #fef0f0;
  color: #f56c6c;
}

.delete-btn:hover {
  background: #fde2e2;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: #fafafa;
  border-radius: 8px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  color: #999;
  margin-bottom: 16px;
}

.empty-state button {
  padding: 8px 16px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  font-size: 24px;
  cursor: pointer;
  color: #999;
  line-height: 1;
}

.modal-body {
  padding: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.checkbox-group input {
  width: auto;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.cancel-btn {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  cursor: pointer;
}

.cancel-btn:hover {
  background: #f5f7fa;
}

.submit-btn {
  padding: 8px 16px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn:hover {
  background: #67b8ff;
}
</style>