import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './features/auth/context/AuthContext';
import { LoginView } from './features/auth/components/LoginView';
import { PublicView } from './features/public/components/PublicView';
import { StaffShell } from './features/staff/checkpoint/components/StaffShell';
import { UserPortalPlaceholder, AdminPortalPlaceholder } from './components/placeholders/PortalPlaceholders';

const RootRedirect: React.FC = () => {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.role === 'ROLE_STAFF') return <Navigate to="/staff" replace />;
  if (user.role === 'ROLE_ADMIN') return <Navigate to="/admin" replace />;
  return <Navigate to="/user" replace />;
};

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<RootRedirect />} />
          <Route path="/login" element={<LoginView />} />
          <Route path="/public" element={<PublicView />} />
          <Route path="/staff/*" element={<StaffShell />} />
          <Route path="/user" element={<UserPortalPlaceholder />} />
          <Route path="/admin" element={<AdminPortalPlaceholder />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;

