import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Lock, User, ShieldCheck, ArrowRight, Loader2, Monitor } from 'lucide-react';
import { Button } from '../../../components/ui/Button';
import { useAuth } from '../context/AuthContext';
import { authApi, DemoAccount } from '../api/authApi';

export const LoginView: React.FC = () => {
  const [username, setUsername] = useState('baove01');
  const [password, setPassword] = useState('password');
  const [demoAccounts, setDemoAccounts] = useState<DemoAccount[]>([]);
  const { login, isLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    authApi.getDemoAccounts().then(setDemoAccounts);
  }, []);

  const handleLogin = async (targetUser?: string) => {
    const userToLogin = targetUser || username;
    const user = await login(userToLogin, password);
    if (user.role === 'ROLE_STAFF') navigate('/staff');
    else if (user.role === 'ROLE_ADMIN') navigate('/admin');
    else navigate('/user');
  };

  return (
    <div className="min-h-screen w-full bg-surface-app flex flex-col justify-between p-6 select-none">
      <header className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-2xl bg-surface-inverse text-text-inverse font-bold text-2xl flex items-center justify-center">P</div>
          <div>
            <h1 className="text-lg font-bold text-text-primary leading-tight">BK Parking Management</h1>
            <p className="text-xs text-text-secondary">Đại học Bách Khoa TP.HCM (HCMUT)</p>
          </div>
        </div>
        <Link to="/public" className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-surface-card border border-border-default text-xs font-bold text-text-primary hover:bg-surface-subtle">
          <Monitor className="w-4 h-4 text-info" /> Xem bảng điện tử Kiosk công khai
        </Link>
      </header>

      <main className="w-full max-w-md mx-auto my-auto flex flex-col gap-5">
        <div className="bg-surface-card border border-border-default rounded-3xl p-6 shadow-xl flex flex-col gap-4">
          <div>
            <h2 className="text-2xl font-bold text-text-primary">Đăng nhập hệ thống</h2>
            <p className="text-xs text-text-secondary mt-1">Cổng xác thực chung cho Cán bộ, Sinh viên &amp; Nhân viên trạm</p>
          </div>

          <form onSubmit={(e) => { e.preventDefault(); handleLogin(); }} className="flex flex-col gap-3">
            <div className="flex flex-col gap-1">
              <label className="text-xs font-bold text-text-secondary">Tên đăng nhập / MSSV / Mã NV:</label>
              <div className="flex items-center gap-2 bg-surface-subtle border border-border-default rounded-xl px-3 py-2.5">
                <User className="w-4 h-4 text-text-secondary shrink-0" />
                <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} className="bg-transparent text-sm font-bold text-text-primary w-full focus:outline-none" required />
              </div>
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-xs font-bold text-text-secondary">Mật khẩu:</label>
              <div className="flex items-center gap-2 bg-surface-subtle border border-border-default rounded-xl px-3 py-2.5">
                <Lock className="w-4 h-4 text-text-secondary shrink-0" />
                <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} className="bg-transparent text-sm font-bold text-text-primary w-full focus:outline-none" required />
              </div>
            </div>

            <Button variant="inverse" disabled={isLoading} icon={isLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : <ArrowRight className="w-4 h-4" />} className="mt-2 w-full">
              {isLoading ? 'Đang xác thực...' : 'Đăng nhập vào hệ thống'}
            </Button>
          </form>

          {/* Chọn tài khoản Demo nhanh */}
          <div className="border-t border-border-subtle pt-3 flex flex-col gap-2">
            <span className="text-xs font-bold text-text-secondary flex items-center gap-1">
              <ShieldCheck className="w-4 h-4 text-info" /> Chọn nhanh tài khoản Demo (1 chạm):
            </span>
            <div className="flex flex-col gap-1.5">
              {demoAccounts.map((acc) => (
                <button
                  key={acc.username}
                  type="button"
                  onClick={() => { setUsername(acc.username); handleLogin(acc.username); }}
                  className="p-2.5 rounded-xl border border-border-default bg-surface-subtle hover:bg-surface-hover text-left flex items-center justify-between text-xs"
                >
                  <div>
                    <span className="font-bold text-text-primary">{acc.fullName}</span>
                    <div className="text-[11px] text-text-secondary mt-0.5">{acc.description}</div>
                  </div>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-surface-card border border-border-default text-info">
                    {acc.role.replace('ROLE_', '')}
                  </span>
                </button>
              ))}
            </div>
          </div>
        </div>
      </main>

      <footer className="text-center text-xs text-text-secondary">
        BK Parking System · ĐHBK HCMUT · CO3011 Project HK261
      </footer>
    </div>
  );
};
