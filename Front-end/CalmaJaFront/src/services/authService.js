import axios from 'axios';

const apiUrl = "http://localhost:8080"

 export const login = async (data) => {
    const response = await axios.post(`${apiUrl}/login`, data)
    return response.data;
 }

 export const register = async (data) => {
    await axios.post(`${apiUrl}/register`, data)
 }

 export const refreshToken = async (refreshToken) => {
    const response = await axios.post(`${apiUrl}/refresh`, {refreshToken})
    return response.data
 }

 export const getCurrentUser = async (token) => {
    const response = await axios.get(`${apiUrl}/users/current`, {
        headers: {Authorization: `Bearer ${token}`}
    })
    return response.data
 }