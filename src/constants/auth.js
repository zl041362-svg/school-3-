export const AUTH_TOKEN_KEY = 'AUTH_TOKEN'
export const AUTH_USER_KEY = 'AUTH_USER'

export const ROLES = {
  GUEST: 'guest',
  CUSTOMER: 'customer',
  FARMER: 'farmer',
  ADMIN: 'admin',
}

export const ROLE_HOME_MAP = {
  [ROLES.ADMIN]: '/admin',
  [ROLES.FARMER]: '/merchant',
  [ROLES.CUSTOMER]: '/',
  [ROLES.GUEST]: '/',
}
