import { Link } from "react-router";
import { CheckCircle } from "lucide-react";

export function ApplySuccess() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[70vh] p-8 text-center">
      <div className="w-20 h-20 bg-[#E8F8F0] rounded-full flex items-center justify-center mb-6">
        <CheckCircle className="w-10 h-10 text-[#34C759]" strokeWidth={1.75} />
      </div>
      <h1 className="text-[28px] text-[#1D1D1F] tracking-tight mb-3" style={{ fontWeight: 600 }}>Application Submitted</h1>
      <p className="text-[16px] text-[#86868B] max-w-sm leading-relaxed">
        Your application has been received. We'll notify you by email once there's an update.
      </p>

      <div className="flex flex-col sm:flex-row gap-3 mt-10">
        <Link
          to="/status"
          className="px-7 py-3 bg-[#0071E3] text-white rounded-full text-[14px] hover:bg-[#0077ED] transition-all active:scale-95 shadow-[0_2px_12px_rgba(0,113,227,0.28)]"
          style={{ fontWeight: 500 }}
        >
          View My Applications
        </Link>
        <Link
          to="/"
          className="px-7 py-3 bg-[#F5F5F7] text-[#1D1D1F] rounded-full text-[14px] hover:bg-[#E8E8ED] transition-all active:scale-95"
          style={{ fontWeight: 500 }}
        >
          Back to Jobs
        </Link>
      </div>
    </div>
  );
}
