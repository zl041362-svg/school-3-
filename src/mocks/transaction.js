export const mockCartItems = [
  {
    id: 1,
    productId: 101,
    name: '五常有机大米',
    qty: 2,
    price: 28,
    stock: 120,
  },
]

export const mockOrders = [
  {
    id: 'OD-1001',
    amount: 56,
    status: 'pending_shipment',
    receiver: '张三',
    phone: '13800000000',
    address: '黑龙江省哈尔滨市道里区',
    paymentStatus: '已支付',
    logistics: '待填写运单号',
    items: [{ id: 1, name: '五常有机大米', qty: 2, price: 28 }],
    createdAt: '2026-05-15 11:30',
  },
]
