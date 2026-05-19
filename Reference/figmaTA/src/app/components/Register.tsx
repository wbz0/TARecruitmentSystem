import { useState } from "react";
import { Link } from "react-router";
import { User, Shield, GraduationCap, Check, ShieldCheck } from "lucide-react";

export function Register() {
  const [userType, setUserType] = useState<"ta" | "mo" | "admin">("ta");

  return (
    <div className="min-h-screen bg-[#F5F5F7] flex flex-col items-center justify-center p-6">
      {/* Logo mark */}
      <div className="mb-8 flex flex-col items-center">
        <div className="w-14 h-14 bg-white rounded-2xl shadow-[0_4px_24px_rgba(0,0,0,0.08)] flex items-center justify-center mb-5">
          <GraduationCap className="w-7 h-7 text-[#0071E3]" strokeWidth={1.75} />
        </div>
        <h1 className="text-[28px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Create Account</h1>
        <p className="text-[15px] text-[#86868B] mt-1">Join the TA Hiring Portal</p>
      </div>

      {/* Card */}
      <div className="w-full max-w-md bg-white rounded-3xl shadow-[0_4px_40px_rgba(0,0,0,0.08)] p-8">
        <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
          {/* User Type Selection */}
          <div className="flex flex-col gap-1.5">
            <label className="text-[13px] text-[#86868B] ml-1" style={{ fontWeight: 500 }}>Account Type</label>
            <div className="grid grid-cols-3 gap-3">
              <button
                type="button"
                onClick={() => setUserType("ta")}
                className={`relative flex items-center justify-center gap-2 rounded-xl py-3.5 text-[14px] transition-all active:scale-95 ${
                  userType === "ta"
                    ? "bg-[#0071E3] text-white shadow-[0_2px_12px_rgba(0,113,227,0.3)]"
                    : "bg-[#F5F5F7] text-[#1D1D1F] hover:bg-[#E8E8ED]"
                }`}
                style={{ fontWeight: 500 }}
              >
                <User className="w-4 h-4" strokeWidth={1.75} />
                TA
                {userType === "ta" && (
                  <div className="absolute right-2 top-1/2 -translate-y-1/2">
                    <Check className="w-4 h-4" strokeWidth={2} />
                  </div>
                )}
              </button>
              <button
                type="button"
                onClick={() => setUserType("mo")}
                className={`relative flex items-center justify-center gap-2 rounded-xl py-3.5 text-[14px] transition-all active:scale-95 ${
                  userType === "mo"
                    ? "bg-[#0071E3] text-white shadow-[0_2px_12px_rgba(0,113,227,0.3)]"
                    : "bg-[#F5F5F7] text-[#1D1D1F] hover:bg-[#E8E8ED]"
                }`}
                style={{ fontWeight: 500 }}
              >
                <Shield className="w-4 h-4" strokeWidth={1.75} />
                MO
                {userType === "mo" && (
                  <div className="absolute right-2 top-1/2 -translate-y-1/2">
                    <Check className="w-4 h-4" strokeWidth={2} />
                  </div>
                )}
              </button>
              <button
                type="button"
                onClick={() => setUserType("admin")}
                className={`relative flex items-center justify-center gap-2 rounded-xl py-3.5 text-[14px] transition-all active:scale-95 ${
                  userType === "admin"
                    ? "bg-[#0071E3] text-white shadow-[0_2px_12px_rgba(0,113,227,0.3)]"
                    : "bg-[#F5F5F7] text-[#1D1D1F] hover:bg-[#E8E8ED]"
                }`}
                style={{ fontWeight: 500 }}
              >
                <ShieldCheck className="w-4 h-4" strokeWidth={1.75} />
                Admin
                {userType === "admin" && (
                  <div className="absolute right-2 top-1/2 -translate-y-1/2">
                    <Check className="w-4 h-4" strokeWidth={2} />
                  </div>
                )}
              </button>
            </div>
          </div>

          {/* Name Fields */}
          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1.5">
              <label className="text-[13px] text-[#86868B] ml-1" style={{ fontWeight: 500 }}>First Name</label>
              <input
                type="text"
                placeholder="John"
                className="w-full bg-[#F5F5F7] border border-transparent rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:bg-white focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all"
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-[13px] text-[#86868B] ml-1" style={{ fontWeight: 500 }}>Last Name</label>
              <input
                type="text"
                placeholder="Doe"
                className="w-full bg-[#F5F5F7] border border-transparent rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:bg-white focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all"
              />
            </div>
          </div>

          {/* Email */}
          <div className="flex flex-col gap-1.5">
            <label className="text-[13px] text-[#86868B] ml-1" style={{ fontWeight: 500 }}>Email address</label>
            <input
              type="email"
              placeholder="name@university.edu"
              className="w-full bg-[#F5F5F7] border border-transparent rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:bg-white focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all"
            />
          </div>

          {/* Student ID (for TA only) */}
          {userType === "ta" && (
            <div className="flex flex-col gap-1.5">
              <label className="text-[13px] text-[#86868B] ml-1" style={{ fontWeight: 500 }}>Student ID</label>
              <input
                type="text"
                placeholder="20240001"
                className="w-full bg-[#F5F5F7] border border-transparent rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:bg-white focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all"
              />
            </div>
          )}

          {/* Department (for MO only) */}
          {userType === "mo" && (
            <div className="flex flex-col gap-1.5">
              <label className="text-[13px] text-[#86868B] ml-1" style={{ fontWeight: 500 }}>Department</label>
              <select
                className="w-full bg-[#F5F5F7] border border-transparent rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] focus:bg-white focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all"
              >
                <option value="">Select department</option>
                <option value="cs">Computer Science</option>
                <option value="math">Mathematics</option>
                <option value="physics">Physics</option>
                <option value="engineering">Engineering</option>
              </select>
            </div>
          )}

          {/* Access Code (for Admin only) */}
          {userType === "admin" && (
            <div className="flex flex-col gap-1.5">
              <label className="text-[13px] text-[#86868B] ml-1" style={{ fontWeight: 500 }}>Admin Access Code</label>
              <input
                type="text"
                placeholder="Enter admin access code"
                className="w-full bg-[#F5F5F7] border border-transparent rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:bg-white focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all"
              />
            </div>
          )}

          {/* Password */}
          <div className="flex flex-col gap-1.5">
            <label className="text-[13px] text-[#86868B] ml-1" style={{ fontWeight: 500 }}>Password</label>
            <input
              type="password"
              placeholder="••••••••"
              className="w-full bg-[#F5F5F7] border border-transparent rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:bg-white focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all"
            />
          </div>

          {/* Confirm Password */}
          <div className="flex flex-col gap-1.5">
            <label className="text-[13px] text-[#86868B] ml-1" style={{ fontWeight: 500 }}>Confirm Password</label>
            <input
              type="password"
              placeholder="••••••••"
              className="w-full bg-[#F5F5F7] border border-transparent rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:bg-white focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all"
            />
          </div>

          {/* Terms and Conditions */}
          <div className="flex items-start gap-2.5 ml-1 mt-1">
            <input
              id="terms"
              type="checkbox"
              className="w-4 h-4 rounded accent-[#0071E3] cursor-pointer mt-0.5"
            />
            <label htmlFor="terms" className="text-[13px] text-[#86868B] cursor-pointer select-none leading-relaxed">
              I agree to the <a href="#" className="text-[#0071E3] hover:underline">Terms and Conditions</a> and <a href="#" className="text-[#0071E3] hover:underline">Privacy Policy</a>
            </label>
          </div>

          {/* Submit Button */}
          <div className="pt-2">
            <Link
              to={userType === "ta" ? "/" : userType === "mo" ? "/mo" : "/admin"}
              className="flex items-center justify-center gap-2 w-full bg-[#0071E3] text-white rounded-xl py-3.5 text-[15px] hover:bg-[#0077ED] transition-all active:scale-95 shadow-[0_2px_12px_rgba(0,113,227,0.3)]"
              style={{ fontWeight: 600 }}
            >
              Create Account
            </Link>
          </div>
        </form>
      </div>

      {/* Sign In Link */}
      <p className="mt-6 text-[13px] text-[#86868B]">
        Already have an account?{" "}
        <Link to="/login" className="text-[#0071E3] hover:underline" style={{ fontWeight: 500 }}>
          Sign in
        </Link>
      </p>

      <p className="mt-4 text-[13px] text-[#AEAEB2]">University Hiring System © 2025</p>
    </div>
  );
}