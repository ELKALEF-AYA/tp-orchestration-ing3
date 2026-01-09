import axios from "axios";
import { ORDER_API_BASE_URL } from "../config";

export const getAllOrders = async () => {
    const res = await axios.get(ORDER_API_BASE_URL);
    return res.data;
};

export const getOrderById = async (id) => {
    const res = await axios.get(`${ORDER_API_BASE_URL}/${id}`);
    return res.data;
};

export const createOrder = async (orderRequest) => {
    const res = await axios.post(ORDER_API_BASE_URL, orderRequest);
    return res.data;
};

export const updateOrderStatus = async (id, newStatus) => {
    const res = await axios.put(`${ORDER_API_BASE_URL}/${id}/status`, {
        status: newStatus,
    });
    return res.data;
};


export const cancelOrder = async (id) => {
    await axios.delete(`${ORDER_API_BASE_URL}/${id}`);
};
