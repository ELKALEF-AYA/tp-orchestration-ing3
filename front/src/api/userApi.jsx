import axios from "axios";
import { USER_API_BASE_URL } from "../config";

export const getAllUsers = async () => {
    const res = await axios.get(USER_API_BASE_URL);
    return res.data;
};

export const createUser = async (userRequest) => {
    const res = await axios.post(USER_API_BASE_URL, userRequest);
    return res.data;
};

export const getUserById = async (id) => {
    const res = await axios.get(`${USER_API_BASE_URL}/${id}`);
    return res.data;
};
