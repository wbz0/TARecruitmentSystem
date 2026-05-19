import { Link } from "react-router";
import { User, Shield, GraduationCap, ShieldCheck } from "lucide-react";

export function Login() {
  return (
    <div className="min-h-screen bg-[#F5F5F7] flex flex-col items-center justify-center p-6">
      {/* Logo mark */}
      <div className="mb-8 flex flex-col items-center">
        <div className="w-14 h-14 bg-white rounded-2xl shadow-[0_4px_24px_rgba(0,0,0,0.08)] flex items-center justify-center mb-5">
          <GraduationCap className="w-7 h-7 text-[#0071E3]" strokeWidth={1.75} />
        </div>
        <h1 className="text-[28px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>TA Hiring Portal</h1>
        <p className="text-[15px] text-[#86868B] mt-1">Sign in to your account</p>
      </div>

      {/* Card */}
      <div className="w-full max-w-sm bg-white rounded-3xl shadow-[0_4px_40px_rgba(0,0,0,0.08)] p-8">
        <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
          <div className="flex flex-col gap-1.5">
            <label className="text-[13px] text-[#86868B] ml-1" style={{ fontWeight: 500 }}>Email address</label>
            <input
              type="email"
              placeholder="name@university.edu"
              className="w-full bg-[#F5F5F7] border border-transparent rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:bg-white focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all"
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <div className="flex items-center justify-between ml-1">
              <label className="text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>Password</label>
              <a href="#" className="text-[13px] text-[#0071E3] hover:underline" style={{ fontWeight: 500 }}>Forgot?</a>
            </div>
            <input
              type="password"
              placeholder="••••••••"
              className="w-full bg-[#F5F5F7] border border-transparent rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:bg-white focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all"
            />
          </div>

          <div className="flex items-center gap-2.5 ml-1 mt-1">
            <input
              id="remember"
              type="checkbox"
              className="w-4 h-4 rounded accent-[#0071E3] cursor-pointer"
            />
            <label htmlFor="remember" className="text-[13px] text-[#86868B] cursor-pointer select-none">Keep me signed in</label>
          </div>

          <div className="pt-2 grid grid-cols-3 gap-3">
            <Link
              to="/"
              className="flex items-center justify-center gap-2 bg-[#F5F5F7] text-[#1D1D1F] rounded-xl py-3 text-[14px] hover:bg-[#E8E8ED] transition-all active:scale-95"
              style={{ fontWeight: 500 }}
            >
              <User className="w-4 h-4" strokeWidth={1.75} />
              TA
            </Link>
            <Link
              to="/mo"
              className="flex items-center justify-center gap-2 bg-[#F5F5F7] text-[#1D1D1F] rounded-xl py-3 text-[14px] hover:bg-[#E8E8ED] transition-all active:scale-95"
              style={{ fontWeight: 500 }}
            >
              <Shield className="w-4 h-4" strokeWidth={1.75} />
              MO
            </Link>
            <Link
              to="/admin"
              className="flex items-center justify-center gap-2 bg-[#0071E3] text-white rounded-xl py-3 text-[14px] hover:bg-[#0077ED] transition-all active:scale-95 shadow-[0_2px_12px_rgba(0,113,227,0.3)]"
              style={{ fontWeight: 500 }}
            >
              <ShieldCheck className="w-4 h-4" strokeWidth={1.75} />
              Admin
            </Link>
          </div>
        </form>
      </div>

      {/* Register Link */}
      <p className="mt-6 text-[13px] text-[#86868B]">
        Don't have an account?{" "}
        <Link to="/register" className="text-[#0071E3] hover:underline" style={{ fontWeight: 500 }}>
          Create account
        </Link>
      </p>

      <p className="mt-4 text-[13px] text-[#AEAEB2]">University Hiring System © 2025</p>
    </div>
  );
}