import { createContext, useState, useEffect } from "react";
import { login, register, refreshToken, getCurrentUser } from "../services/authService";
import api from "../services/api";

export const AuthContext = createContext()

export const AuthProvider = ({children}) => {
    const [user, setUser] = useState(null)
    const [token, setToken] = useState(localStorage.getItem('token'))
    const [refreshToken, setRefreshToken] = useState(localStorage.getItem('refreshToken'))
    const [error, setError] = useState('')
    
    useEffect(() => {
        if(token) loadCurrentUser()
    }, [token])

    const logout = () => {
        setUser(null)
        setToken(null)
        setRefreshToken(null)
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('token')
    }

    const loadCurrentUser = async () => {
        try{
            const userData = await getCurrentUser(token)
            setUser(userData)
        }catch (err) {
            logout()
        }
    }

    const handleLogin = async (data) => {
        try{
            const {token, refreshToken} = await login(data)
            setToken(token)
            setRefreshToken(refreshToken)
            localStorage.setItem('token', token);
            localStorage.setItem('refreshToken', refreshToken);
            await loadCurrentUser();
            setError('')
        }catch(err){
            setError('Usuário ou Senha Inválidos')
        }
    }

    const handleRegister = async (data) => {
        try{
            await register(data)
            setError('')
        }catch (err){
            setError('Erro ao criar conta')
        }
    }
    

    api.interceptors.response.use(
        (response) => response,
        async(error) => {
            if(error.response?.status === 401 && token && refreshToken){
                try{
                    const {token: newToken} = await refreshToken(refreshToken)
                    setToken(newToken)
                    localStorage.setItem('token', newToken)
                    error.config.header['Authorization'] = `Bearer ${newToken}`
                    return api.request(error.config)
            
                }catch(refreshError) {
                    logout()
                    return Promise.reject(refreshError)   
                }
            }
            return Promise.reject(error)
        }
    )

    return (
        <AuthContext.Provider value={{user, token, error, login: handleLogin, register: handleRegister, logout}}>
            {children}
        </AuthContext.Provider>
    )
}