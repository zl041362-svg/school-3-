export const mockFarmerVerifications = [
  {
    id: 1,
    farmer: '黑土地农场',
    submittedAt: '2026-05-15 09:00',
    status: 'pending',
    contact: '13800001111',
  },
  {
    id: 2,
    farmer: '赣南果园合作社',
    submittedAt: '2026-05-14 14:20',
    status: 'pending',
    contact: '13900002222',
  },
]

export const mockProductReviews = [
  {
    id: 201,
    product: '安溪铁观音',
    farmer: '闽茶基地',
    submittedAt: '2026-05-15 10:20',
    status: 'pending',
    price: 66,
  },
]

export const mockNewsReviews = [
  {
    id: 401,
    title: '春耕行情追踪',
    author: '平台农讯组',
    submittedAt: '2026-05-15 11:00',
    status: 'pending',
  },
]

export const mockAdminProducts = [
  {
    id: 101,
    name: '五常有机大米',
    farmer: '黑土地农场',
    status: 'published',
    price: 28,
  },
  {
    id: 102,
    name: '安溪铁观音',
    farmer: '闽茶基地',
    status: 'pending',
    price: 66,
  },
]

export const mockAdminNews = [
  {
    id: 301,
    title: '本周市场快讯',
    author: '平台农讯组',
    status: 'published',
    publishedAt: '2026-05-15 08:00',
  },
  {
    id: 302,
    title: '赣南脐橙产地纪实',
    author: '赣南果园合作社',
    status: 'pending',
    publishedAt: '2026-05-15 13:00',
  },
]

export const mockAdminUsers = [
  {
    id: 1,
    name: '平台管理员',
    role: 'admin',
    status: 'active',
    createdAt: '2026-05-01 09:00',
  },
  {
    id: 2,
    name: '黑土地农场',
    role: 'farmer',
    status: 'active',
    createdAt: '2026-05-12 14:30',
  },
  {
    id: 3,
    name: '普通消费者',
    role: 'customer',
    status: 'disabled',
    createdAt: '2026-05-14 19:20',
  },
]

export const mockAdminRoles = [
  {
    id: 1,
    role: 'admin',
    members: 2,
    description: '平台全局管理与配置权限',
  },
  {
    id: 2,
    role: 'auditor',
    members: 1,
    description: '负责商品、资讯、认证审核',
  },
]

export const mockAdminPermissions = [
  {
    id: 1,
    module: 'product-review',
    action: 'approve',
    role: 'auditor',
  },
  {
    id: 2,
    module: 'news-review',
    action: 'reject',
    role: 'auditor',
  },
  {
    id: 3,
    module: 'rbac',
    action: 'manage',
    role: 'admin',
  },
]

export const mockAdminLogs = [
  {
    id: 1,
    operator: 'admin',
    action: 'review_product',
    createdAt: '2026-05-15 10:00:00',
    detail: '通过商品 安溪铁观音',
  },
  {
    id: 2,
    operator: 'auditor',
    action: 'review_news',
    createdAt: '2026-05-15 11:10:00',
    detail: '驳回资讯 春耕行情追踪',
  },
]
