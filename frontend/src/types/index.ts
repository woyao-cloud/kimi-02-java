// User types
export interface User {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  fullName: string;
  active: boolean;
  verified: boolean;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt?: string;
  roles: Role[];
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  roleIds?: string[];
}

export interface UpdateUserRequest {
  email?: string;
  firstName?: string;
  lastName?: string;
  active?: boolean;
  roleIds?: string[];
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

// Role types
export interface Role {
  id: string;
  name: string;
  description?: string;
  defaultRole: boolean;
  createdAt: string;
  permissions: Permission[];
}

export interface CreateRoleRequest {
  name: string;
  description?: string;
  defaultRole?: boolean;
  permissionIds?: string[];
}

export interface UpdateRoleRequest {
  description?: string;
  defaultRole?: boolean;
  permissionIds?: string[];
}

// Permission types
export interface Permission {
  id: string;
  name: string;
  resource: string;
  action: string;
  createdAt: string;
}

export interface CreatePermissionRequest {
  name: string;
  resource: string;
  action: string;
}

// Auth types
export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserInfo;
}

export interface UserInfo {
  id: string;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
  permissions: string[];
}

export interface CurrentUserResponse {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  fullName: string;
  active: boolean;
  verified: boolean;
  lastLoginAt?: string;
  roles: string[];
  permissions: string[];
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

// Pagination
export interface PageMeta {
  page: number;
  size: number;
  total: number;
  totalPages?: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}
