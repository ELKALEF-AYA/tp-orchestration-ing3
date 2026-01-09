import axios from "axios";
import { PRODUCT_API_BASE_URL } from "../config";

export const getAllProducts = async () => {
    const res = await axios.get(PRODUCT_API_BASE_URL);
    return res.data;
};

export const getProductById = async (id) => {
    const res = await axios.get(`${PRODUCT_API_BASE_URL}/${id}`);
    return res.data;
};

export const createProduct = async (payload) => {
    const res = await axios.post(PRODUCT_API_BASE_URL, payload, {
        headers: { "Content-Type": "application/json" },
    });
    return res.data;
};
export const updateProduct = async (id, payload) => {
    const res = await axios.put(`${PRODUCT_API_BASE_URL}/${id}`, payload, {
        headers: { "Content-Type": "application/json" },
    });
    return res.data;
};

export const deleteProduct = async (id) => {
    const res = await axios.delete(`${PRODUCT_API_BASE_URL}/${id}`);
    return res.data;
};