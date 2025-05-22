import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import Navbar from './components/common/Navbar';
import FooterComponent from './components/common/Footer';
import HomePage from './components/homepage/HomePage';
import LoginPage from './components/auth/LoginPage';
import RegistrationPage from './components/auth/RegistrationPage';
import RequireAdmin from './components/auth/RequireAdmin';
import RequireUser from './components/auth/RequireUser';
import ProfilePage from './components/userpage/ProfilePage';
import UserManagementPage from './components/userpage/UserManagementPage';
import UpdateUser from './components/userpage/UpdateUser';
import Events from './components/events/Events';
import EventList from './components/events/EventList';
import CreateEventForm from './components/events/CreateEventForm';
import CartPage from './components/cart/CartPage';
import { CartProvider } from './context/CartContext';
import { AuthProvider } from './context/AuthContext'; // Import the AuthProvider
import GuestOnlyRoute from './components/GuestOnlyRoute/GuestOnlyRoute';

import DebugAuth from './context/DebugAuth';


function App() {
  return (
    <BrowserRouter>
      <AuthProvider> {/* Wrap everything with AuthProvider */}
        <CartProvider>
          <div className="App">
            <Navbar />
            <DebugAuth /> {/* Ajouté ici */}
            <div className="content">

              
              <Routes>
                {/* Public routes */}
                <Route path="/" element={<HomePage />} />

                {/* Guest-only routes */}
                <Route
                  path="/login"
                  element={
                    <GuestOnlyRoute>
                      <LoginPage />
                    </GuestOnlyRoute>
                  }
                />
                <Route
                  path="/register"
                  element={
                    <GuestOnlyRoute>
                      <RegistrationPage />
                    </GuestOnlyRoute>
                  }
                />

                {/* Admin registration */}
                <Route
                  path="/admin/register"
                  element={
                    <RequireAdmin>
                      <RegistrationPage adminRegistration={true} />
                    </RequireAdmin>
                  }
                />

                {/* User routes */}
                <Route
                  path="/profile"
                  element={
                    <RequireUser>
                      <ProfilePage />
                    </RequireUser>
                  }
                />
                <Route
                  path="/public-events"
                  element={
                    <RequireUser>
                      <Events />
                    </RequireUser>
                  }
                />
                <Route
                  path="/cart"
                  element={
                    <RequireUser>
                      <CartPage />
                    </RequireUser>
                  }
                />

                {/* Admin routes */}
                <Route
                  path="/admin/events"
                  element={
                    <RequireAdmin>
                      <EventList />
                    </RequireAdmin>
                  }
                />
                <Route
                  path="/admin/user-management"
                  element={
                    <RequireAdmin>
                      <UserManagementPage />
                    </RequireAdmin>
                  }

                />
                <Route
                  path="/admin/update-user/:userId"
                  element={
                    <RequireAdmin>
                      <UpdateUser />
                    </RequireAdmin>
                  }
                />


                <Route
                  path="/admin/create-event"
                  element={
                    <RequireAdmin>
                      <CreateEventForm />
                    </RequireAdmin>
                  }
                />

                {/* Catch-all route */}
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </div>
            <FooterComponent />
          </div>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;