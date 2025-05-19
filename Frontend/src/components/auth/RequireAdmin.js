// src/components/auth/RequireAdmin.js
import React from 'react';
import { Navigate } from 'react-router-dom';
import UsersService from '../services/UsersService';

const RequireAdmin = ({ children }) => {
  if (!UsersService.adminOnly()) {
    return <Navigate to="/login" />;
  }
  return children;
};

export default RequireAdmin;
