import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import Navbar from './components/common/Navbar';
import FooterComponent from './components/common/Footer';

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
import Homepage from './components/homepage/HomePage';

function App() {
  return (
    <BrowserRouter>
      <CartProvider>
        <div className="App">
          <Navbar />
          <div className="content">
            <Routes>
              {/* Page d'accueil publique */}
              <Route path="/home" element={<Homepage />} />

              {/* Page de login accessible à tous */}
              <Route path="/" element={<LoginPage />} />
              <Route path="/login" element={<LoginPage />} />

              {/* Pages protégées utilisateurs authentifiés */}
              <Route path="/profile" element={
                <RequireUser>
                  <ProfilePage />
                </RequireUser>
              } />

              {/* Routes admin protégées */}
              <Route path="/events" element={
                <RequireAdmin>
                  <EventList />
                </RequireAdmin>
              } />
              <Route path="/register" element={
                <RequireAdmin>
                  <RegistrationPage />
                </RequireAdmin>
              } />
              <Route path="/admin/user-management" element={
                <RequireAdmin>
                  <UserManagementPage />
                </RequireAdmin>
              } />
              <Route path="/update-user/:userId" element={
                <RequireAdmin>
                  <UpdateUser />
                </RequireAdmin>
              } />
              <Route path="/create-event" element={
                <RequireAdmin>
                  <CreateEventForm />
                </RequireAdmin>
              } />

              {/* Routes utilisateur simple protégées */}
              <Route path="/public-events" element={
                <RequireUser>
                  <Events />
                </RequireUser>
              } />
              <Route path="/cart" element={
                <RequireUser>
                  <CartPage />
                </RequireUser>
              } />

              {/* Redirection pour routes inconnues */}
              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </div>
          <FooterComponent />
        </div>
      </CartProvider>
    </BrowserRouter>
  );
}

export default App;
