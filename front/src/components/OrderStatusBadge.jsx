import React from "react";

const STATUS_META = {
    PENDING: { label: "EN ATTENTE", css: "badge badge-yellow" },
    CONFIRMED: { label: "CONFIRMÉE", css: "badge badge-blue" },
    SHIPPED: { label: "EXPÉDIÉE", css: "badge badge-purple" },
    DELIVERED: { label: "LIVRÉE", css: "badge badge-green" },
    CANCELLED: { label: "ANNULÉE", css: "badge badge-red" },
};

const OrderStatusBadge = ({ status }) => {
    if (!status) return <span className="badge">UNKNOWN</span>;

    const normalized = String(status).toUpperCase();
    const meta = STATUS_META[normalized];

    if (!meta) return <span className="badge">{normalized}</span>;

    return <span className={meta.css}>{meta.label}</span>;
};

export default OrderStatusBadge;