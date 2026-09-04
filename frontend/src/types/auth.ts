import { User } from './user';

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export interface LoginDTO {
  email: string;
  password: string;
}

export interface RegisterDTO {
  fullName: string;
  email: string;
  password: string;
}
