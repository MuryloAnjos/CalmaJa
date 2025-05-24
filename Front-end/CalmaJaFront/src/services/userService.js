import axios from "axios";

const apiUrl = "http://localhost:8080/users"

const getAuthConfig = (token) => ({
    headers: { Authorization: `Bearer ${token}`}
})

export const getCurrentUser = async (token) => {
    const response = axios.get(`${apiUrl}/current`, getAuthConfig(token))
    return response.data
}