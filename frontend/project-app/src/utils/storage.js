// src/utils/storage.js

/* -----------------------------
   ACCESS TOKEN
------------------------------ */
const ACCESS_TOKEN_KEY = "accessToken";

export function setAccessToken(token) {
  localStorage.setItem(ACCESS_TOKEN_KEY, token);
}

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}


/* -----------------------------
   REFRESH TOKEN
   (FE에서 보관해야 백엔드 /api/auth/refresh 연동 가능)
------------------------------ */
const REFRESH_TOKEN_KEY = "refreshToken";

export function setRefreshToken(token) {
  localStorage.setItem(REFRESH_TOKEN_KEY, token);
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}


/* -----------------------------
   CLEAR TOKENS
------------------------------ */
export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}
