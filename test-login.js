const axios = require('axios');

async function testLogin() {
    try {
        const response = await axios.post('http://localhost:8080/api/auth/login', {
            username: 'admin',
            password: 'admin123'
        });
        
        console.log('登录成功!');
        console.log('Token:', response.data.token);
        console.log('用户信息:', response.data);
    } catch (error) {
        console.error('登录失败:', error.response?.data || error.message);
    }
}

testLogin();
