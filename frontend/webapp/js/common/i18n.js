/*
 * AppI18n 全站静态文案字典。
 *
 * 登录、注册、首页和各角色门户都通过 data-i18n 或 AppI18n.t 读取这里的文案。
 * 纯动态服务端错误会先走 localizeServerMessage，再退回服务端英文原文。
 */
(function () {
    var STORAGE_KEY = "ta_hiring_locale";
    var DEFAULT_LOCALE = "en";
    var CHINESE_LOCALE = "zh-CN";

    var dictionaries = {
        "en": {
            common: {
                portalBrand: "TA Hiring System",
                utility: {
                    backToPortal: "Portal home"
                },
                locale: {
                    switchAria: "Switch language",
                    zh: "中文",
                    en: "English"
                },
                action: {
                    signIn: "Sign in",
                    createAccount: "Create account",
                    createAdmin: "Create admin account"
                },
                footer: {
                    copyright: "TA Hiring System © 2026"
                },
                password: {
                    show: "Show password",
                    hide: "Hide password"
                }
            },
            index: {
                page: {
                    title: "TA Hiring System - Home"
                },
                nav: {
                    aria: "Main navigation",
                    overview: "Overview",
                    forTa: "For TA",
                    forMo: "For MO",
                    forAdmin: "For Admin",
                    process: "Process",
                    faq: "FAQ"
                },
                hero: {
                    badge: "Role-based TA hiring system",
                    title: "Coordinate TA profiles, postings, reviews, and workload",
                    subtitle: "A role-based system where TAs apply, MOs review applications, and admins manage workload, invite codes, and announcements.",
                    primary: "Get started",
                    secondary: "Sign in",
                    adminHint: "Need admin access?",
                    adminLink: "Use admin invite code"
                },
                preview: {
                    title: "Current project modules at a glance",
                    subtitle: "The homepage reflects the role pages and API flows implemented in this project.",
                    workflowAriaLabel: "Portal workflow preview",
                    jobKicker: "Open TA role",
                    jobMeta: "2 positions · 8 hours / week",
                    reviewKicker: "MO review queue",
                    candidateStrong: "Recommended applicant",
                    candidateReview: "Manual review",
                    adminKicker: "TA workload",
                    activeApplications: "accepted TA work",
                    workflowTitle: "Live role flow",
                    workflowSubtitle: "CSV-backed Servlet/JSP project",
                    taLaneTitle: "Applicant workspace",
                    taLaneMeta: "Profile, jobs, applications",
                    taItem1: "Profile and resume ready",
                    taItem2: "Open jobs available",
                    taItem3: "Application status tracked",
                    moLaneTitle: "Organizer review",
                    moLaneMeta: "Postings, applicants, decisions",
                    moItem1: "Published TA postings",
                    moItem2: "Applicant list by posting",
                    moItem3: "Accept or reject applications",
                    adminLaneTitle: "Admin operations",
                    adminLaneMeta: "Workload, invite code, notices",
                    adminItem1: "Accepted TA workload",
                    adminItem2: "8-character invite code",
                    adminItem3: "System announcements",
                    stateReady: "Ready",
                    stateOpen: "Open",
                    stateReview: "Review",
                    stateDecision: "Decision",
                    stateActive: "Active",
                    stateLive: "Live",
                    cardTaTitle: "TA workspace",
                    cardTaDesc: "Maintain a profile, upload resume/photo, browse openings, apply, and track application status.",
                    cardMoTitle: "MO workspace",
                    cardMoDesc: "Publish postings, manage your jobs, review applications, and accept or reject candidates.",
                    cardAdminTitle: "Admin workspace",
                    cardAdminDesc: "Review accepted TA workload, refresh invite codes, and publish announcements."
                },
                forTa: {
                    title: "For teaching assistants",
                    lead: "Everything a TA needs from profile setup to application status tracking.",
                    item1: "Build and update your profile with resume and skills.",
                    item2: "Search open positions, or request optional AI job recommendations.",
                    item3: "Submit applications, open job details, and check pending, accepted, rejected, or withdrawn updates.",
                    cta: "Sign in as TA"
                },
                forMo: {
                    title: "For module organizers",
                    lead: "Publish openings, manage your postings, and review applications from one workflow.",
                    item1: "Create and maintain postings with course, skills, slots, workload, salary, and deadline.",
                    item2: "Open applicant lists from your postings and review profiles, resumes, and cover letters.",
                    item3: "Accept or reject applications, with optional AI recommendations and analysis as support.",
                    cta: "Sign in as MO"
                },
                forAdmin: {
                    title: "For administrators",
                    lead: "Manage the operational pieces that support the hiring workflow.",
                    item1: "Review accepted TA workload by TA, job, course, weekly hours, and active period.",
                    item2: "View or refresh the current 8-character admin invite code.",
                    item3: "Publish announcements that TA, MO, and Admin users can read.",
                    cta: "Sign in as Admin"
                },
                process: {
                    title: "From registration to final offer",
                    lead: "The homepage mirrors the current end-to-end process in the system.",
                    step1Title: "1. Register account",
                    step1Desc: "TA/MO use standard registration. Admin accounts are created with an 8-character invite code.",
                    step2Title: "2. Complete profile or post job",
                    step2Desc: "TAs prepare profile details. MOs publish openings with requirements and deadlines.",
                    step3Title: "3. Apply and review",
                    step3Desc: "TAs submit applications. MOs review applicants and make selection decisions.",
                    step4Title: "4. Track status and workload",
                    step4Desc: "TAs monitor outcomes, MOs complete decisions, and admins review accepted TA workload."
                },
                ai: {
                    title: "Optional AI recommendation and analysis",
                    lead: "AI is an add-on to TA and MO workflows, not a separate workflow.",
                    item1: "TAs can request job recommendations based on their profile and open positions.",
                    item2: "MOs can request applicant recommendations for their published jobs.",
                    item3: "TA/MO detail pages can request analysis; if AI is unavailable, manual review still works."
                },
                faq: {
                    title: "Frequently asked questions",
                    q1: "Do I need to visit this page every time?",
                    a1: "No. Returning users can open the login page directly and continue from there.",
                    q2: "Which role should I choose?",
                    a2: "Choose TA for applicants, MO for module organizers, and Admin only for platform managers with an invite code.",
                    q3: "Can I switch language later?",
                    a3: "Yes. Use the top-right language switch at any time. Your choice is remembered."
                },
                cta: {
                    title: "Ready to start your TA hiring workflow?",
                    subtitle: "Use this portal for context, then jump to the sign-in flow you need.",
                    primary: "Sign in now",
                    secondary: "Create account"
                }
            },
            login: {
                page: {
                    title: "Login - TA Hiring System"
                },
                hero: {
                    title: "TA Hiring System",
                    subtitle: "Sign in to your account"
                },
                form: {
                    aria: "Login form",
                    usernameLabel: "Username or email",
                    usernamePlaceholder: "username or name@university.edu",
                    passwordLabel: "Password",
                    passwordPlaceholder: "Enter your password",
                    forgot: "Forgot?",
                    keepSignedIn: "Keep me signed in",
                    roleLabel: "Sign in as",
                    roleAria: "Role selection buttons",
                    ta: "TA",
                    taDesc: "Applicant",
                    mo: "MO",
                    moDesc: "Module Organizer",
                    admin: "Admin",
                    adminDesc: "Manager",
                    submit: "Log in"
                },
                links: {
                    noAccount: "Don't have an account?",
                    createAccount: "Create one now",
                    needAdmin: "Need admin access?",
                    createAdmin: "Use admin invitation"
                },
                msg: {
                    failed: "Login failed. Please check your username and password.",
                    successRedirect: "Login successful! Redirecting...",
                    credentialError: "Username/email or password is incorrect.",
                    roleError: "Role selection error.",
                    loggingIn: "Logging in...",
                    enterIdentifier: "Please enter your username or email.",
                    identifierTooLong: "Username or email is too long.",
                    identifierUnsupported: "Username or email contains unsupported characters.",
                    invalidEmail: "Please enter a valid email address.",
                    invalidUsername: "Username must start with a letter and contain 3-20 letters, numbers, or underscores.",
                    enterPassword: "Please enter your password.",
                    passwordTooShort: "Password must be at least 8 characters.",
                    passwordTooLong: "Password is too long.",
                    passwordUnsupported: "Password contains unsupported characters.",
                    networkError: "Network error. Please try again."
                }
            },
            register: {
                page: {
                    title: "Register - TA Hiring System"
                },
                hero: {
                    title: "Create your account",
                    subtitle: "Join TA Hiring System in a few steps"
                },
                form: {
                    aria: "Registration form",
                    usernameLabel: "Username",
                    usernamePlaceholder: "john_smith",
                    usernameHint: "3-20 characters, start with a letter, and use only letters, numbers, or underscores.",
                    usernameInfoAria: "Username rules",
                    usernameTooltip: "3-20 chars, start with a letter, letters/numbers/underscore. No consecutive __ or trailing _.",
                    emailLabel: "Email address",
                    emailPlaceholder: "name@university.edu",
                    emailInfoAria: "Email rules",
                    emailTooltip: "Enter a valid email address (e.g. name@university.edu).",
                    passwordLabel: "Password",
                    passwordPlaceholder: "Create a password",
                    passwordHint: "Use at least 8 characters.",
                    passwordInfoAria: "Password rules",
                    passwordTooltip: "At least 8 characters, including at least one letter and one number.",
                    passwordTooSimple: "Password must contain at least one letter and one number.",
                    confirmLabel: "Confirm password",
                    confirmPlaceholder: "Re-enter your password",
                    confirmInfoAria: "Confirm password rules",
                    confirmTooltip: "Re-enter the password you created above.",
                    roleLabel: "Register as",
                    roleAria: "Role selection buttons",
                    roleTaTitle: "TA",
                    roleTaDesc: "Applicant",
                    roleMoTitle: "MO",
                    roleMoDesc: "Module Organizer",
                    submit: "Create account"
                },
                links: {
                    haveAccount: "Already have an account?",
                    backLogin: "Back to login",
                    adminQuestion: "Need an Admin account?",
                    adminLink: "Use admin invitation"
                },
                msg: {
                    enterUsername: "Please enter a username.",
                    usernameTooLong: "Username is too long.",
                    usernameUnsupported: "Username contains unsupported characters.",
                    usernameInvalid: "Must start with a letter, 3-20 letters/numbers/underscores.",
                    usernameConsecutiveUnderscore: "Username cannot contain consecutive underscores.",
                    usernameTrailingUnderscore: "Username cannot end with an underscore.",
                    usernameUnavailable: "Username is already taken.",
                    enterEmail: "Please enter your email address.",
                    emailTooLong: "Email is too long.",
                    emailUnsupported: "Email contains unsupported characters.",
                    emailInvalid: "Please enter a valid email address.",
                    emailUnavailable: "Email is already registered.",
                    enterPassword: "Please create a password.",
                    passwordTooShort: "Password must be at least 8 characters.",
                    passwordTooLong: "Password is too long.",
                    passwordUnsupported: "Password contains unsupported characters.",
                    passwordTooSimple: "Password must contain at least one letter and one number.",
                    enterConfirmPassword: "Please confirm your password.",
                    passwordMismatch: "Passwords do not match.",
                    selectRole: "Please select a role.",
                    adminUsePage: "Admin accounts are created from the invite code page.",
                    failed: "Registration failed. Please check your information and try again.",
                    successRedirect: "Registration successful! Redirecting to login...",
                    networkError: "Network error. Please try again."
                }
            },
            adminInvite: {
                page: {
                    title: "Admin Invitation - TA Hiring System"
                },
                hero: {
                    title: "Complete admin invitation",
                    subtitle: "Use an invite code from the team to create an Admin account"
                },
                contactHint: {
                    intro: "To get an invite code, send an email from the address you plan to register with to the contact below. You will receive the invite code in reply.",
                    contactLabel: "Contact:",
                    contactEmail: "admin@example.com"
                },
                form: {
                    aria: "Admin invitation form",
                    emailLabel: "Email address",
                    emailPlaceholder: "admin@university.edu",
                    emailInfoAria: "Email address requirements",
                    emailTooltip: "Enter the email address you will register with.",
                    inviteCodeLabel: "Invite code",
                    inviteCodePlaceholder: "ABCDEFGH",
                    inviteCodeInfoAria: "Invite code help",
                    inviteCodeTooltip: "Enter the 8-character invite code provided by an admin.",
                    usernameLabel: "Username",
                    usernamePlaceholder: "admin_username",
                    usernameInfoAria: "Username requirements",
                    usernameTooltip: "3-20 chars, start with a letter, letters/numbers/underscore. No consecutive __ or trailing _.",
                    passwordLabel: "Password",
                    passwordPlaceholder: "Create a password",
                    passwordInfoAria: "Password requirements",
                    passwordTooltip: "At least 8 characters, including at least one letter and one number.",
                    confirmLabel: "Confirm password",
                    confirmPlaceholder: "Re-enter your password",
                    confirmInfoAria: "Confirm password requirements",
                    confirmTooltip: "Re-enter your password to confirm.",
                    submit: "Create admin account"
                },
                links: {
                    haveAccount: "Already have an account?",
                    backLogin: "Back to login"
                },
                msg: {
                    passwordTooShort: "Password must be at least 8 characters.",
                    passwordTooLong: "Password is too long.",
                    passwordMismatch: "Passwords do not match.",
                    createFailed: "Failed to create admin account.",
                    createSuccessRedirect: "Admin account created. Redirecting to login...",
                    networkError: "Network error. Please try again.",
                    creating: "Creating...",
                    enterEmail: "Please enter your email address.",
                    emailTooLong: "Email is too long.",
                    emailUnsupported: "Email contains unsupported characters.",
                    emailInvalid: "Please enter a valid email address.",
                    emailUnavailable: "Email is already registered.",
                    enterUsername: "Please enter a username.",
                    usernameTooLong: "Username is too long.",
                    usernameUnsupported: "Username contains unsupported characters.",
                    usernameInvalid: "Must start with a letter, 3-20 letters/numbers/underscores.",
                    usernameConsecutiveUnderscore: "Username cannot contain consecutive underscores.",
                    usernameTrailingUnderscore: "Username cannot end with an underscore.",
                    usernameUnavailable: "Username is already taken.",
                    enterPassword: "Please create a password.",
                    passwordUnsupported: "Password contains unsupported characters.",
                    passwordTooSimple: "Password must contain at least one letter and one number.",
                    enterConfirmPassword: "Please confirm your password.",
                    inviteCodeRequired: "Invite code is required.",
                    inviteCodeInvalidOrExpired: "Invite code is invalid or expired."
                }
            },
            portal: {
                action: {
                    signOut: "Sign Out",
                    switchRoles: "Switch Roles",
                    save: "Save Changes",
                    cancel: "Cancel",
                    edit: "Edit",
                    delete: "Delete",
                    inbox: "Inbox"
                },
                accountProfile: {
                    open: "Edit account display profile",
                    kicker: "Account display",
                    title: "Edit account profile",
                    chooseAvatar: "Choose avatar",
                    avatarHintTa: "This avatar is only for your account display. Application reviewers still see the avatar saved in your TA profile.",
                    avatarHintMo: "This avatar is only for your account display. Job cards use your title and real name below.",
                    nickname: "Nickname",
                    realName: "Real name",
                    professionalTitle: "Title",
                    professionalTitlePlaceholder: "Dr. / Prof.",
                    saved: "Account profile saved.",
                    saveFailed: "Unable to save account profile."
                },
                brand: {
                    ta: "TA Portal",
                    mo: "MO Portal",
                    admin: "Admin Portal"
                },
                nav: {
                    ta: {
                        aria: "TA portal navigation",
                        jobs: "Job List",
                        status: "My Applications",
                        profile: "Profile",
                        notifications: "Notifications"
                    },
                    mo: {
                        aria: "MO portal navigation",
                        myJobs: "My Postings",
                        postJob: "Post Job",
                        notifications: "Notifications"
                    },
                    admin: {
                        aria: "Admin portal navigation",
                        dashboard: "Dashboard",
                        invite: "Invite Code",
                        notifications: "Notifications"
                    }
                },
                page: {
                    taDashboard: {
                        title: "TA Profile Setup - TA Hiring System"
                    },
                    taJobList: {
                        title: "Job list - TA Hiring System"
                    },
                    taJobDetail: {
                        title: "Job detail - TA Hiring System"
                    },
                    taApplicationStatus: {
                        title: "Application status - TA Hiring System"
                    },
                    taApplicationDetail: {
                        title: "Application detail - TA Hiring System"
                    },
                    moDashboard: {
                        title: "MO Dashboard - Post TA Jobs"
                    },
                    adminDashboard: {
                        title: "TA Workload - TA Hiring System"
                    },
                    adminInviteManagement: {
                        title: "Admin Invitation Management - TA Hiring System"
                    },
                    taNotifications: {
                        title: "Notifications - TA Hiring System"
                    },
                    moNotifications: {
                        title: "Notifications - TA Hiring System"
                    },
                    adminNotifications: {
                        title: "Notifications - TA Hiring System"
                    }
                },
                notifications: {
                    subtitle: "Announcements from the admin team",
                    empty: "No announcements yet",
                    composeTitle: "Publish Notification",
                    titleLabel: "Title",
                    titlePlaceholder: "Notification title",
                    contentLabel: "Message",
                    contentPlaceholder: "Write your message here…",
                    publishBtn: "Publish",
                    deleteBtn: "Delete",
                    publishedBy: "Published by",
                    published: "Notification published.",
                    fillAll: "Please fill in both title and message.",
                    deleteFailed: "Failed to delete notification.",
                    publishFailed: "Failed to publish notification.",
                    networkError: "Network error."
                },
                common: {
                    search: "Search",
                    keyword: "Keyword",
                    all: "All",
                    open: "Open",
                    closed: "Closed",
                    filled: "Filled",
                    openUpper: "OPEN",
                    courseCode: "Course code",
                    applyFilters: "Apply filters",
                    clear: "Clear",
                    refresh: "Refresh",
                    close: "Close",
                    positions: "Positions",
                    workload: "Workload",
                    salary: "Salary",
                    deadline: "Deadline",
                    description: "Description",
                    requiredSkills: "Required skills",
                    application: "Application",
                    pending: "Pending",
                    accepted: "Accepted",
                    rejected: "Rejected",
                    withdrawn: "Withdrawn",
                    total: "Total",
                    selectJob: "Select a job",
                    high: "High",
                    medium: "Medium",
                    low: "Low",
                    job: "Job",
                    course: "Course",
                    status: "Status",
                    processed: "Processed",
                    loading: "Loading..."
                },
                taDashboard: {
                    subtitle: "Manage your personal information and academic background.",
                    profileLayoutAria: "TA profile form and saved profile",
                    createProfileTitle: "Create your TA profile",
                    basicDetails: "Basic details",
                    fullName: "Full name",
                    fullNameInfoAria: "Full name format",
                    fullNameTooltip: "Letters, spaces, hyphens, apostrophes, and periods. At least 2 characters.",
                    fullNamePlaceholder: "Your full name",
                    required: "Required",
                    studentId: "Student ID",
                    studentIdInfoAria: "Student ID format",
                    studentIdTooltip: "10-digit number starting with 20, e.g. 2023213039.",
                    studentIdPlaceholder: "e.g. 2023213039",
                    department: "Department",
                    departmentInfoAria: "Department format",
                    departmentTooltip: "Your school or department name, 2–100 characters.",
                    departmentPlaceholder: "School or department",
                    program: "Program",
                    programInfoAria: "Program info",
                    programTooltip: "Select the level that matches your current enrollment.",
                    selectProgram: "Select your program",
                    programUndergraduate: "Undergraduate",
                    programMaster: "Master",
                    programPhd: "PhD",
                    additionalInfo: "Additional information",
                    additionalInfoLead: "These fields are optional for now, but completing them will make your profile stronger.",
                    gpa: "GPA",
                    gpaInfoAria: "GPA format",
                    gpaTooltip: "Enter your GPA, e.g. 3.85 or 3.85/4.00 (value/scale).",
                    gpaPlaceholder: "e.g. 3.85 / 4.00",
                    phone: "Phone number",
                    phoneInfoAria: "Phone number format",
                    phoneTooltip: "8–15 digits, international format accepted, e.g. +86 138 0000 0000.",
                    phonePlaceholder: "+86 138 0000 0000",
                    skills: "Skills",
                    skillsInfoAria: "Skills format",
                    skillsTooltip: "Use English commas or Chinese commas to separate up to 12 skills, e.g. Java, SQL, Python.",
                    skillsPlaceholder: "Separate skills with commas, for example Java, JSP, SQL",
                    skillsHint: "Use English commas or Chinese commas to separate each skill. Your skills will be saved as a list.",
                    experience: "Related experience",
                    experienceInfoAria: "Related experience info",
                    experienceTooltip: "Describe relevant teaching or tutoring experience. At least 10 words.",
                    experiencePlaceholder: "Describe tutoring, teaching, grading, or project experience relevant to a TA role.",
                    motivation: "Motivation",
                    motivationInfoAria: "Motivation info",
                    motivationTooltip: "Explain your motivation for this TA role. At least 10 words.",
                    motivationPlaceholder: "Explain why you want this TA opportunity and what value you can bring.",
                    createProfileButton: "Create profile",
                    saveChangesButton: "Save changes",
                    editProfileButton: "Edit profile",
                    cancelButton: "Cancel",
                    profileLabel: "Personal Profile",
                    profilePhotoAlt: "Profile photo",
                    photoUploadTitle: "Photo upload",
                    photoCardEmptyTitle: "Upload your photo",
                    photoCardEmptyHint: "JPG, PNG, or WEBP. Maximum size is 5MB.",
                    photoRemoveAria: "Remove photo",
                    resumeUploadTitle: "Resume upload",
                    resumeUploadLead: "Upload one PDF, DOC, or DOCX resume. Maximum size is 10MB.",
                    resumeCardEmptyTitle: "Upload your resume",
                    resumeCardEmptyHint: "PDF, DOC, or DOCX. Maximum size is 10MB.",
                    chooseFile: "Choose file",
                    resumeRemoveAria: "Remove resume",
                    noFileSelected: "No file selected.",
                    waitingUpload: "Waiting to upload",
                    resumeDraftTitle: "Save together",
                    createProfileFirst: "Create profile first",
                    resumeTip: "You can upload the resume first or fill the other fields first. The newest file takes effect after you save changes.",
                    uploadSelectedResume: "Upload selected resume",
                    validation: {
                        fullName: {
                            required: "Please enter your full name.",
                            tooLong: "Full name must be 100 characters or fewer.",
                            tooShort: "Full name must be at least 2 characters.",
                            noLetter: "Full name must include at least one letter.",
                            invalidChars: "Full name may only include letters, spaces, apostrophes, periods, and hyphens.",
                            tooManyRepeated: "Full name contains too many repeated characters."
                        },
                        studentId: {
                            required: "Please enter your student ID.",
                            notTenDigits: "Student ID must be exactly 10 digits, for example 2023213039.",
                            notStartWith20: "Student ID should start with 20, for example 2023213051.",
                            invalidYear: "Student ID year appears invalid. Please check the first 4 digits.",
                            allSameDigit: "Student ID appears invalid. Please check your official 10-digit student number."
                        },
                        department: {
                            required: "Please enter your department.",
                            tooLong: "Department must be 100 characters or fewer.",
                            tooShort: "Department must be at least 2 characters.",
                            noLetter: "Department should include letters.",
                            invalidChars: "Department contains unsupported characters.",
                            tooManyRepeated: "Department contains too many repeated characters."
                        },
                        program: {
                            required: "Please select your program.",
                            invalidOption: "Please select a valid program option."
                        },
                        gpa: {
                            required: "Please enter your GPA.",
                            tooLong: "GPA must be 20 characters or fewer.",
                            invalidChars: "GPA may only include digits, spaces, decimal separators, and '/'.",
                            multipleSlash: "GPA format is invalid. Use one optional '/'.",
                            invalidValue: "GPA value supports up to 2 decimal places.",
                            negative: "GPA cannot be negative.",
                            invalidScale: "GPA scale supports up to 2 decimal places.",
                            scaleOutOfRange: "GPA scale should be between 4 and 100.",
                            valueExceedsScale: "GPA value cannot be greater than the GPA scale.",
                            tooHighWithoutScale: "For GPA above 4.3, please include scale (for example 85/100)."
                        },
                        skills: {
                            required: "Please enter at least one skill.",
                            tooLong: "Skills must be 300 characters or fewer.",
                            emptyItems: "Please remove empty skill items between commas.",
                            useCommaSeparator: "Please use English commas or Chinese commas to separate skills.",
                            tooManySkills: "Please list up to 12 skills.",
                            skillLength: "Each skill should be 2 to 40 characters.",
                            noLetter: "Each skill should include letters.",
                            invalidChars: "Skills contain unsupported characters.",
                            tooManyRepeated: "A skill item has too many repeated characters.",
                            duplicate: "Duplicate skills found. Please keep each skill only once."
                        },
                        phone: {
                            required: "Please enter your phone number.",
                            tooLong: "Phone number must be 30 characters or fewer.",
                            invalidChars: "Phone number may only include digits, spaces, and + - ( ) . /.",
                            multiplePlus: "Phone number can contain only one '+'.",
                            plusNotAtStart: "If used, '+' must be at the beginning.",
                            unbalancedParens: "Phone number parentheses are not balanced.",
                            digitCount: "Phone number should contain 8 to 15 digits.",
                            allSameDigit: "Phone number appears invalid. Please check repeated digits.",
                            internationalTooShort: "International format should usually contain at least 10 digits."
                        },
                        experience: {
                            required: "Please describe your related experience.",
                            tooLong: "Related experience must be 1200 characters or fewer.",
                            tooShort: "Related experience should be at least 20 characters.",
                            notEnoughDetail: "Related experience should contain more detail (about 10 words/characters).",
                            tooManyRepeated: "Related experience contains too many repeated characters."
                        },
                        motivation: {
                            required: "Please explain your motivation.",
                            tooLong: "Motivation must be 1200 characters or fewer.",
                            tooShort: "Motivation should be at least 20 characters.",
                            notEnoughDetail: "Motivation should contain more detail (about 10 words/characters).",
                            tooManyRepeated: "Motivation contains too many repeated characters."
                        }
                    }
                },
                taJobList: {
                    subtitle: "Browse and apply for open TA positions.",
                    panelAria: "TA job search and result list",
                    loadingPositions: "Loading positions...",
                    searchPlaceholder: "Search jobs by title, course code, or keywords",
                    aiSearchPlaceholder: "Ask for recommended jobs based on your profile",
                    aiSearchButton: "AI",
                    aiSearching: "AI searching...",
                    aiSearchLoading: "AI is recommending jobs...",
                    aiSearchUnavailable: "AI recommendation is unavailable right now.",
                    aiOutOfScope: "I cannot handle your question. I can recommend jobs, compare jobs, or explain recommendation reasons based on your profile and open positions.",
                    aiRecommendationTitle: "Recommendation (AI generated)",
                    aiRecommendedUnit: "AI recommendation",
                    aiNoRecommendations: "No AI recommendations for the current open positions.",
                    aiNoRecommendationsHint: "Try asking for a different teaching focus or check again when more open jobs are available.",
                    searchModeToggle: "Search mode"
                },
                taJobDetail: {
                    title: "Job Detail",
                    subtitle: "Review role requirements and submit your application.",
                    detailCardAria: "Job detail card",
                    backToJobs: "← Job list",
                    loadingDetails: "Loading job details...",
                    missingId: "Missing job ID. Please return to the list and try again.",
                    moduleOrganizer: "Module organizer",
                    submitApplicationTitle: "Submit your application",
                    applyProfileHint: "When you submit, your profile and cover letter will be sent to the MO together.",
                    coverLetterHint: "Add a short cover letter to highlight your fit for this role.",
                    coverLetterPlaceholder: "Briefly explain your relevant experience, strengths, and availability.",
                    coverLetter: "Cover letter",
                    applyNow: "Apply for this job",
                    onlyTaHint: "Only TA accounts can submit applications. If you have already applied, this panel will show your latest status."
                },
                taApplicationStatus: {
                    title: "My Applications",
                    subtitle: "Track the status of your submitted applications.",
                    panelAria: "Application search and status list",
                    loadingApplications: "Loading applications...",
                    searchPlaceholder: "Search by job title, course code, or MO"
                },
                taApplicationDetail: {
                    title: "Application detail",
                    backToList: "← My applications",
                    jobTeaserTitle: "Applied position details",
                    viewDetailsCta: "View details →",
                    gpaScale: "Out of 4.0",
                    progressTitle: "Application progress",
                    mySkills: "My skills",
                    responsibilities: "Responsibilities",
                    viewResumeFile: "View file",
                    resumeShort: "Resume",
                    closeModal: "Close",
                    missingId: "Missing application ID. Return to the list and try again.",
                    loadAppFailed: "Unable to load application.",
                    networkError: "Network error. Please try again.",
                    untitled: "Untitled position",
                    submittedPrefix: "Submitted on",
                    noSkills: "No skills listed",
                    noCoverLetter: "No cover letter provided.",
                    jobUnavailable: "Job details unavailable.",
                    workload: "Workload",
                    applicants: "Applicants",
                    deadline: "Deadline",
                    deadlinePrefix: "Due",
                    noDescription: "No description.",
                    sessionExpired: "Your session has expired. Redirecting to login...",
                    withdrawAction: "Withdraw application",
                    withdrawing: "Withdrawing...",
                    withdrawConfirm: "Withdraw this application? The MO will see it as withdrawn.",
                    withdrawSuccess: "Application withdrawn successfully.",
                    withdrawFailed: "Unable to withdraw this application.",
                    withdrawUnavailable: "This application cannot be withdrawn.",
                    withdrawNetworkError: "Network error while withdrawing application.",
                    progressSubmit: "Submit application",
                    progressReview: "Materials in review",
                    progressInterview: "Interview arrangement",
                    progressFinal: "Final decision",
                    profileCardTitle: "My profile",
                    profileCardHint: "View or edit your resume and skills.",
                    profileCardHintReady: "View or edit your resume and skills.",
                    profileCardHintMissingResume: "Add or update your resume, skills, and profile details.",
                    profileSyncNote: "Your profile and resume were sent with this application to the MO. You can update your profile after submission, and changes will sync to the MO view.",
                    profileSyncUpdatedPrefix: "Latest profile sync:"
                },
                moDashboard: {
                    title: "Post New Job",
                    subtitle: "Create a new TA position listing for your course.",
                    myJobsHeroSubtitle: "View and manage the TA job postings you have published.",
                    myJobsPanelAria: "My posted jobs",
                    postJobPanelAria: "Post a new job form",
                    createPosting: "Create posting",
                    manage: "Manage",
                    postPosition: "Post a new TA position",
                    jobTitle: "Job title",
                    jobTitlePlaceholder: "e.g. Teaching Assistant - Data Structures",
                    courseCodePlaceholder: "e.g. EBU6304",
                    courseNamePlaceholder: "e.g. Software Engineering",
                    descriptionPlaceholder: "Describe responsibilities, expectations, and any course-specific requirements.",
                    requiredSkillsPlaceholder: "Use English or Chinese commas only, e.g. Java, SQL, communication",
                    workloadPlaceholder: "e.g. 8 hours / week",
                    weeklyHoursPlaceholder: "e.g. 8",
                    salaryPlaceholder: "e.g. 25 RMB / hour",
                    required: "Required",
                    requiredLead: "Fields labeled Required are required for publishing.",
                    hintAria: "Field help",
                    hint: {
                        title: "Up to 200 characters, no HTML tags.",
                        courseCode: "Start with a letter or number, e.g. EBU6304. Up to 50 characters, no spaces.",
                        courseName: "Full course name, up to 120 characters.",
                        description: "Describe responsibilities and requirements in detail, up to 4000 characters.",
                        requiredSkills: "Use English commas or Chinese commas only, up to 20 items, e.g. Java, SQL, Git.",
                        positions: "Number of positions, an integer from 1 to 200.",
                        deadline: "Must be later than the current time and within 2 years.",
                        weeklyHours: "Weekly workload, 0.5 to 40 hours, at most one decimal place.",
                        workStartDate: "Cannot be before the application deadline date.",
                        workEndDate: "Must not be earlier than the work start date.",
                        salary: "Free text, e.g. 25 RMB / hour, up to 120 characters."
                    },
                    courseInfo: "Course information",
                    roleRequirements: "Role requirements",
                    hiringSettings: "Hiring settings",
                    jobTitleRequired: "Job title *",
                    courseCodeRequired: "Course code *",
                    courseName: "Course name",
                    applicationDeadline: "Application deadline",
                    weeklyHours: "Weekly hours",
                    workStartDate: "Work start date",
                    workEndDate: "Work end date",
                    publishJob: "Publish job",
                    publishing: "Publishing...",
                    resetForm: "Reset form",
                    myPostings: "My postings",
                    myJobs: "My Postings",
                    postNew: "Post New Job",
                    myJobsDesc: "View and manage your job postings.",
                    publishedJobs: "Published jobs",
                    loadingJobs: "Loading your jobs...",
                    noJobsTitle: "No job postings yet",
                    noJobsDesc: "Click \"Post New Job\" to create your first TA position listing.",
                    editJob: "Edit Job",
                    confirmDelete: "Confirm Delete",
                    deleteConfirmMsg: "Are you sure you want to delete this job posting?",
                    userNotLoggedIn: "User not logged in.",
                    failedLoadJobs: "Failed to load jobs.",
                    jobNotFound: "Job not found.",
                    failedUpdateJob: "Failed to update job.",
                    jobUpdatedSuccess: "Job updated successfully.",
                    failedDeleteJob: "Failed to delete job.",
                    jobDeletedSuccess: "Job deleted successfully.",
                    deleting: "Deleting...",
                    noDeadline: "No deadline",
                    validationJobTitleRequired: "Job title is required.",
                    validationJobTitleLength: "Job title must be 200 characters or fewer.",
                    validationJobTitleUnsupported: "Job title contains unsupported characters.",
                    validationCourseCodeRequired: "Course code is required.",
                    validationCourseCodeLength: "Course code must be 50 characters or fewer.",
                    validationCourseCodeUnsupported: "Course code contains unsupported characters.",
                    validationCourseNameRequired: "Course name is required.",
                    validationCourseNameLength: "Course name must be 120 characters or fewer.",
                    validationCourseNameUnsupported: "Course name contains unsupported characters.",
                    validationDescriptionRequired: "Description is required.",
                    validationDescriptionLength: "Description must be 4000 characters or fewer.",
                    validationDescriptionUnsupported: "Description contains unsupported characters.",
                    validationSkillsRequired: "Required skills are required.",
                    validationSkillsLength: "Required skills must be 500 characters or fewer.",
                    validationSkillsUnsupported: "Required skills contain unsupported characters.",
                    validationSkillsEmpty: "Please remove empty skill items.",
                    validationSkillsCommaSeparator: "Please use English commas or Chinese commas to separate skills.",
                    validationSkillsDuplicate: "Duplicate skills found. Please keep each skill only once.",
                    validationSkillsLimit: "Please list up to 20 skills.",
                    validationPositionsRequired: "Positions must be a whole number.",
                    validationPositionsNumber: "Positions must be a whole number.",
                    validationPositionsRange: "Positions must be between 1 and 200.",
                    validationWorkloadRequired: "Workload is required.",
                    validationWorkloadLength: "Workload must be 120 characters or fewer.",
                    validationWorkloadUnsupported: "Workload contains unsupported characters.",
                    validationWeeklyHoursRequired: "Weekly hours are required.",
                    validationWeeklyHoursNumber: "Weekly hours must be a number with at most one decimal place.",
                    validationWeeklyHoursRange: "Weekly hours must be between 0.5 and 40.",
                    validationWorkDateRequired: "{field} is required.",
                    validationWorkDateInvalid: "{field} must use yyyy-MM-dd.",
                    validationWorkStartBeforeDeadline: "Work start date cannot be before application deadline.",
                    validationWorkPeriodOrder: "Work end date cannot be before work start date.",
                    validationSalaryRequired: "Salary is required.",
                    validationSalaryLength: "Salary must be 120 characters or fewer.",
                    validationSalaryUnsupported: "Salary contains unsupported characters.",
                    validationDeadlineRequired: "Application deadline is required.",
                    validationDeadlineInvalid: "Invalid deadline format.",
                    validationDeadlinePast: "Deadline cannot be in the past.",
                    validationDeadlineTooFar: "Deadline cannot be more than 2 years in the future.",
                    backToMyJobs: "← My postings",
                    backToApplicants: "← Applicant list"
                },
                moApplicantSelection: {
                    subtitle: "Review and manage all candidate applications.",
                    panelAria: "Applicant selection panel",
                    detailPanelAria: "Applicant detail panel",
                    loadingApplications: "Loading applications...",
                    searchPlaceholder: "Search by applicant name, email, or job title",
                    aiSearchButton: "AI",
                    aiSearching: "AI...",
                    aiSearchLoading: "AI searching...",
                    aiSearchUnavailable: "AI search is currently unavailable.",
                    aiOutOfScope: "我无法处理您的问题。我可以根据当前职位的申请人信息，帮你推荐候选人、比较申请人或解释推荐理由。",
                    aiRecommendationTitle: "推荐建议（AI生成）",
                    aiRecommendedUnit: "AI recommendation(s)",
                    searchModeToggle: "Switch search mode",
                    job: "Job",
                    allJobs: "All jobs",
                    applicantProfile: "Applicant profile",
                    selectApplicant: "Select an applicant",
                    viewResume: "View resume",
                    academic: "Academic",
                    studentId: "Student ID",
                    department: "Department",
                    program: "Program",
                    gpa: "GPA",
                    contact: "Contact",
                    email: "Email",
                    phone: "Phone",
                    address: "Address",
                    application: "Application",
                    skills: "Skills",
                    experience: "Experience",
                    coverLetter: "Cover letter",
                    profileSyncedAt: "Profile synced at",
                    profileSync: "Profile sync",
                    profileSyncHint: "The profile will sync automatically after the applicant saves changes.",
                    backToCourseList: "Back to course list",
                    backToApplicantList: "Back to applicant list",
                    courseApplicants: "Applicants",
                    applicationDetail: "Application detail",
                    applicationDetailLead: "Review applicant profile and complete your decision.",
                    openCourseApplicants: "View applicants for",
                    courseOwnerFallback: "Module owner unavailable",
                    applicantsLabel: "Applicants",
                    jobCountLabel: "Jobs",
                    noJobTitle: "No job title available",
                    unknownApplicant: "Unknown applicant",
                    openApplicationDetail: "Open application detail for",
                    appliedAtLabel: "Applied at",
                    progressLabel: "Progress",
                    unknownCourseCode: "Unknown course",
                    courseUnit: "course",
                    courseOverviewMoLabel: "Module organizer",
                    courseOverviewRolesLabel: "Posted TA roles",
                    courseOverviewTaPostingsLabel: "TA postings",
                    courseOverviewTaPostingsTitle: "How many TA job postings exist for this course (distinct listings).",
                    courseOverviewApplicationsLabel: "Applications",
                    courseOverviewApplicationsTitle: "Total applications submitted across all TA postings for this course.",
                    courseOverviewAcceptedTitle: "Applications marked as accepted for this course.",
                    courseOverviewRejectedTitle: "Applications marked as rejected for this course.",
                    courseOverviewWorkloadTitle: "Weekly workload from the posting (for example hours per week).",
                    courseOverviewDeadlineTitle: "Earliest application deadline across TA postings for this course.",
                    courseOverviewNoDeadline: "Not set",
                    courseOverviewDescriptionHeading: "Job description",
                    courseOverviewNoDescription: "No description available.",
                    courseOverviewSkillsHeading: "Required skills",
                    courseOverviewNoSkills: "No required skills listed.",
                    hireChipVisual: "Hire",
                    rejectChipVisual: "Reject",
                    applicationMaterials: "Application materials",
                    resumeDocument: "Resume",
                    resumeFormatHint: "Uploaded file",
                    viewAction: "View",
                    hireApplicant: "Hire applicant",
                    rejectApplicant: "Reject",
                    processing: "Processing...",
                    alreadyReviewed: "This application has already been reviewed.",
                    noCoverLetter: "No cover letter provided.",
                    profileUnavailable: "Applicant profile details are temporarily unavailable.",
                    motivationLabel: "Motivation",
                    noSkillsListed: "No skills listed",
                    noExperience: "No experience provided.",
                    noMotivation: "No motivation statement provided.",
                    resumeNotUploaded: "Resume not uploaded",
                    ariaLabel: "Applicant list"
                },
                adminDashboard: {
                    title: "TA Workload",
                    subtitle: "Track accepted TA job workload by weekly hours and active work period.",
                    panelAria: "TA workload dashboard",
                    start: "Work range start",
                    end: "Work range end",
                    searchLabel: "Search",
                    searchPlaceholder: "Search by TA name, job title, or course code",
                    applyRange: "Apply range",
                    exportCsv: "Export CSV",
                    inviteTitle: "Invite Code",
                    inviteLead: "Share the current code with applicants who request admin access.",
                    codePanel: {
                        title: "Current invite code",
                        subtitle: "Copy this code and reply to the applicant's email.",
                        displayAria: "Current admin invite code",
                        loading: "Loading...",
                        refreshBtn: "Force refresh",
                        loadError: "Failed to load invite code.",
                        refreshError: "Failed to refresh code.",
                        workflowTitle: "How it works",
                        step1: "Applicant emails the team contact address from the email they plan to register with.",
                        step2: "Check this page for the current invite code.",
                        step3: "Reply to the applicant's email with the invite code.",
                        step4: "Applicant enters the code on the registration page to create their admin account."
                    },
                    taCount: "TA Count",
                    acceptedJobs: "Accepted Jobs",
                    totalWeeklyHours: "Weekly Hours",
                    totalWorkWeeks: "Work Weeks",
                    totalWorkHours: "Total Work Hours",
                    invalidJobs: "Invalid Jobs",
                    taWorkloadOverview: "TA Workload Ranking",
                    taWorkloadLead: "Accepted workload hours by TA in the selected work range.",
                    taWorkload: "TA Workload Details",
                    includedWorkloadPanel: "Included Workload",
                    includedWorkloadPanelAria: "Included TA workload cards",
                    includedWorkloadLead: "TA cards are sorted by total accepted workload. Click a card to view the counted jobs.",
                    paginationAria: "TA workload pagination",
                    pageButtonAria: "Page",
                    filteredSummary: "Filtered from",
                    pageSummaryPrefix: "Showing",
                    noWorkloadMatches: "No TA workload matches your keyword.",
                    noWorkloadMatchesHint: "Try another TA name, job title, or course code.",
                    viewDetails: "View details",
                    collapseDetails: "Hide details",
                    invalidJobPanel: "Jobs Excluded From Stats",
                    invalidJobPanelAria: "Jobs excluded from workload statistics",
                    invalidJobLead: "Accepted jobs that cannot be counted are listed here, such as jobs missing weekly hours, work start/end dates, or valid field formats.",
                    noInvalidJobs: "No accepted jobs are excluded.",
                    noInvalidJobsTitle: "Why this section exists",
                    noInvalidJobsHint: "This section is kept as a data-quality check. Accepted jobs missing weekly hours, work start/end dates, or valid field formats will appear here.",
                    hoursPerWeek: "hours/week",
                    weeks: "weeks",
                    hours: "hours",
                    loadingWorkload: "Loading workload..."
                },
                dynamic: {
                    checkingProfile: "Checking profile...",
                    savingChanges: "Saving changes...",
                    profileAlreadyExists: "A profile already exists for this account. Loading your saved profile...",
                    fixHighlightedFields: "Please fix the highlighted fields and try again.",
                    noProfileFound: "No profile found yet. Please complete the form below.",
                    unableCheckProfile: "Unable to check your existing profile right now. You can still try creating one.",
                    unableCreateProfile: "Unable to create your profile. Please review the form and try again.",
                    unableUpdateProfile: "Unable to update your profile. Please review the form and try again.",
                    profileCreatedUploadingResume: "Profile created. Uploading your selected resume...",
                    profileCreatedResumeFailed: "Profile created, but resume upload failed. Please try uploading again.",
                    profileCreatedSuccess: "Profile created successfully. Your saved information is now displayed below.",
                    profileUpdatedSuccess: "Profile updated successfully.",
                    profileReadonly: "Your profile has already been created and is now shown in read-only mode.",
                    currentResumePrefix: "Current uploaded resume:",
                    noResumeUploaded: "No resume uploaded yet.",
                    noResumeSelected: "No resume file selected.",
                    chooseResumeFirst: "Please choose a resume file first.",
                    resumeRequiredToSave: "Please upload your resume before saving your profile.",
                    resumeDraftUploading: "Uploading resume draft:",
                    resumeDraftSaved: "Resume draft uploaded. Save changes to apply it.",
                    resumeDraftReplaceSaved: "New resume uploaded. Save changes to replace the current resume.",
                    pendingResumeRemoved: "Pending resume removed.",
                    savedResumeRemoved: "Current resume removed. Upload a new one before saving changes.",
                    resumeReady: "Resume ready",
                    choosePhotoFirst: "Please choose a photo file first.",
                    photoReadyToSave: "Photo selected. Save changes to apply it.",
                    savedPhotoRemoved: "Current photo removed. Save changes to apply it.",
                    photoReady: "Photo ready",
                    pendingResumePrefix: "Pending resume:",
                    pendingResumeCreateSuffix: " It will be saved when you create the profile.",
                    pendingResumeReplaceSuffix: " It will replace your current resume after you save.",
                    resumeDiscardFailed: "Unable to discard the pending resume. Please try again.",
                    createProfileThenUpload: "Please create your profile first, then upload the resume.",
                    createProfileAutoUpload: "Please create your profile first. The selected resume will also upload automatically after creation.",
                    resumeWillUploadAfterCreate: "Will upload after profile creation",
                    resumeReadyAfterCreate: "Resume file is ready and will upload right after profile creation.",
                    replaceUploadedResume: "Replace uploaded resume",
                    resumeReadyReplace: "Resume file is ready. Click upload to replace your current resume.",
                    uploading: "Uploading",
                    uploadCompleted: "Upload completed",
                    uploadAborted: "Upload aborted.",
                    uploadInterrupted: "Upload was interrupted. Please try again.",
                    uploadNetworkError: "Network error during file upload. Please try again.",
                    resumeUploadSuccess: "Resume uploaded successfully.",
                    resumeUpdateSuccess: "Resume updated successfully.",
                    resumeUploadFailed: "Resume upload failed. Please try again.",
                    invalidResumeFormat: "Invalid file format. Please upload a PDF, DOC, or DOCX file.",
                    resumeTooLarge: "File size exceeds 10MB. Please choose a smaller file.",
                    invalidPhotoFormat: "Invalid photo format. Please upload JPG, PNG, or WEBP.",
                    photoTooLarge: "Photo size exceeds 5MB. Please choose a smaller file.",
                    resumeWaitingUpload: "Waiting to upload",
                    noSpecificSkills: "No specific skills listed.",
                    searching: "Searching...",
                    unableLoadJobs: "Unable to load jobs right now.",
                    unableLoadJobsRetry: "Unable to load jobs right now. Please try again.",
                    noJobsForFilters: "No jobs found for the current filters.",
                    noJobsForSearch: "No jobs match your keyword.",
                    noJobsAvailable: "No jobs available right now.",
                    showing: "Showing",
                    jobUnit: "job",
                    unableLoadPositionsTitle: "Unable to load positions",
                    refreshAfterNetworkCheck: "Please refresh the list after checking your network connection.",
                    noPositionsPublishedTitle: "No positions published yet",
                    positionsAppearAfterPublish: "When MO publishes new jobs, they will appear here.",
                    noMatchingPositionsTitle: "No matching positions",
                    closestMatchesNotice: "No exact matches. Showing closest results.",
                    tryAnotherKeyword: "Try another keyword.",
                    broadenKeywordHint: "Try broadening your keyword or clearing one filter.",
                    noExtraTags: "No extra tags",
                    viewDetails: "View details",
                    applyNow: "Apply now",
                    moShort: "MO",
                    submitting: "Submitting...",
                    jobIdMissing: "Job ID is missing.",
                    applicationSubmitted: "Application has been submitted.",
                    applicationSubmittedRedirect: "Application submitted successfully. Redirecting to application status...",
                    failedSubmitApplication: "Failed to submit application. Please try again.",
                    unableLoadJobDetailsNow: "Unable to load job details right now.",
                    currentAccountCannotSubmit: "Current account cannot submit applications on this page.",
                    onlyTaSubmit: "Only TA accounts can submit applications.",
                    alreadyApplied: "You have already applied for this job.",
                    jobNoLongerAvailable: "This job is no longer available.",
                    jobNotAccepting: "This job is not accepting new applications.",
                    positionCurrently: "This position is currently",
                    newApplicationsDisabled: ". New applications are disabled.",
                    jobNotFound: "Job not found. It may have been removed.",
                    applicationUnavailable: "Application unavailable",
                    applicationStatusPrefix: "Application status:",
                    applicationAlreadySubmitted: "Application already submitted.",
                    applicationStopped: "Applications closed",
                    cannotSubmitMissingJobId: "Cannot submit because job ID is missing.",
                    coverLetterControlChars: "Cover letter contains unsupported control characters.",
                    coverLetterUnsupportedMarkup: "Cover letter contains unsupported markup.",
                    coverLetterTooLong: "Cover letter must be 2000 characters or fewer.",
                    networkErrorSubmitApplication: "Network error while submitting application.",
                    taOnlyPage: "This page is available for TA accounts only.",
                    unableLoadApplications: "Unable to load your applications.",
                    unableLoadApplicationsNow: "Unable to load applications right now.",
                    noApplicationsSubmitted: "No applications submitted yet.",
                    noApplicationsForSearch: "No applications match your keyword.",
                    noApplicationsForPostedJobs: "No applications submitted for your jobs yet.",
                    noApplicationsForPostedJobsHint: "Once TAs apply for your posted jobs, applicant cards will appear here.",
                    noApplicationsMatchFilters: "No applications match the current filters.",
                    applicationUnit: "application",
                    unableLoadApplicationsTitle: "Unable to load applications",
                    noMatchingApplicationsTitle: "No matching applications",
                    noApplicationsYetTitle: "No applications yet",
                    applicationTimeline: "Application timeline",
                    applied: "Applied",
                    review: "Review",
                    decision: "Decision",
                    statusAppearsAfterApply: "After you apply for a job, the status will appear here.",
                    clearFiltersToBroaden: "Try clearing status or keyword filters to broaden results.",
                    applicationWithdrawnSuccess: "Application withdrawn successfully.",
                    unableWithdrawApplication: "Unable to withdraw this application.",
                    networkErrorWithdrawApplication: "Network error while withdrawing application.",
                    appliedAt: "Applied at",
                    coverLetterColon: "Cover letter:",
                    noCoverLetterProvided: "No cover letter provided.",
                    viewJob: "View job",
                    withdraw: "Withdraw",
                    onlyMoPublish: "Only MO accounts can publish jobs.",
                    validationDeadlineTooFar: "Deadline cannot be more than 2 years in the future.",
                    backToMyJobs: "← My postings",
                    backToApplicants: "← Applicant list",
                    failedPublishJob: "Failed to publish job. Please check your input and try again.",
                    jobPostedSuccess: "Job posted successfully.",
                    networkErrorPostingJob: "Network error while posting job.",
                    unableLoadPostings: "Unable to load postings right now.",
                    noJobsPostedYet: "No jobs posted yet.",
                    youHavePosted: "You have posted",
                    noPostingsYetTitle: "No postings yet",
                    publishFirstTaPosition: "Use the form to publish your first TA position.",
                    reviewApplicants: "Review applicants",
                    untitledPosition: "Untitled position",
                    overviewPartialLoad: "Some overview data could not be loaded. Showing available results.",
                    unableLoadOverview: "Unable to load overview data right now.",
                    moOnlyPage: "This page is available for MO accounts only.",
                    noActivityYet: "No activity yet.",
                    tracking: "Tracking",
                    noRecentActivityTitle: "No recent activity",
                    latestUpdatesAppear: "Once TAs apply for your jobs, latest updates will appear here.",
                    newApplicationReceived: "New application received",
                    offerAccepted: "Offer accepted",
                    applicationAccepted: "Application accepted.",
                    applicationRejected: "Application rejected.",
                    applicationWithdrawn: "Application withdrawn",
                    applicationUpdated: "Application updated",
                    unknownApplicant: "Unknown applicant",
                    failedLoadApplicationTotals: "Failed to load application totals.",
                    failedLoadMoWorkloads: "Failed to load MO workloads.",
                    failedLoadTaWorkloads: "Failed to load TA workloads.",
                    networkErrorLoadingDashboard: "Network error while loading dashboard.",
                    exporting: "Exporting...",
                    csvExportedSuccess: "CSV exported successfully.",
                    unableExportCsv: "Unable to export CSV.",
                    noMoWorkloadSelectedRange: "No MO workload data in selected range.",
                    noTaWorkloadSelectedRange: "No TA workload data in selected work range.",
                    loaded: "Loaded",
                    moWorkloadItemUnit: "MO workload item",
                    taWorkloadItemUnit: "TA workload item",
                    noStatusData: "No status data available.",
                    noMoWorkloadData: "No MO workload data available.",
                    noTaWorkloadData: "No TA workload data available.",
                    noWorkloadDataYetTitle: "No workload data yet",
                    adjustTimeRangeHint: "Adjust time range or wait for application activity to appear.",
                    adjustWorkRangeHint: "Adjust the work range or wait for accepted jobs to appear.",
                    sessionExpiredRedirect: "Session expired. Redirecting to login...",
                    invalidDatetimeFormat: "Invalid datetime format.",
                    startAfterEnd: "Start time cannot be after end time.",
                    networkErrorTryAgain: "Network error. Please try again.",
                    networkErrorMoment: "Network error. Please try again in a moment.",
                    currentCompleteness: "Current completeness:"
                }
            },
            server: {
                auth: {
                    loginRequired: "Please log in first.",
                    unauthorized: "Please log in first.",
                    forbidden: "You do not have permission to access this resource.",
                    accessDenied: "Access denied.",
                    noResourcePermission: "You do not have permission to access this resource.",
                    noActionPermission: "You do not have permission to perform this action.",
                    invalidRoleParameter: "Invalid role parameter.",
                    roleMismatch: "Selected role does not match this account.",
                    invalidCredentials: "Username/email or password is incorrect.",
                    loginError: "An error occurred during login. Please try again later.",
                    loginSuccess: "Login successful.",
                    logoutSuccess: "Logout successful.",
                    registrationSuccess: "Registration successful.",
                    usernameRequired: "Username is required.",
                    usernameTooLong: "Username is too long.",
                    usernameInvalid: "Username format is invalid.",
                    usernameConsecutiveUnderscore: "Username cannot contain consecutive underscores.",
                    usernameTrailingUnderscore: "Username cannot end with an underscore.",
                    usernameExists: "Username is already taken.",
                    emailRequired: "Email address is required.",
                    emailTooLong: "Email address is too long.",
                    emailInvalid: "Please enter a valid email address.",
                    emailExists: "Email address is already registered.",
                    passwordRequired: "Password is required.",
                    passwordTooShort6: "Password must be at least 6 characters.",
                    passwordTooShort8: "Password must be at least 8 characters.",
                    passwordTooLong: "Password is too long.",
                    passwordTooSimple: "Password must contain at least one letter and one number.",
                    passwordMismatch: "Passwords do not match."
                },
                adminInvite: {
                    codeRequired: "Invite code is required.",
                    codeInvalidOrExpired: "Invite code is invalid or expired.",
                    accountCreated: "Admin account created.",
                    accountCreateFailed: "Failed to create admin account.",
                    adminAccessRequired: "Admin access is required.",
                    codeRotated: "Invite code refreshed."
                },
                account: {
                    profileRetrieved: "Account profile loaded.",
                    profileUpdated: "Account profile updated.",
                    onlyTaMoUpdate: "Only TA or MO accounts can update account profile.",
                    displayNameTooLong: "Nickname is too long.",
                    realNameTooLong: "Real name is too long.",
                    professionalTitleTooLong: "Title is too long.",
                    unsupportedChars: "Account profile contains unsupported characters.",
                    avatarTooLarge: "Avatar file is too large.",
                    avatarInvalidType: "Avatar must be JPG, PNG, or WEBP."
                },
                applicant: {
                    profileNotFound: "Applicant profile not found.",
                    profileRetrieved: "Applicant profile retrieved.",
                    profileCreated: "Applicant profile created.",
                    profileUpdated: "Applicant profile updated.",
                    profileExistsForUser: "Applicant profile already exists for this account.",
                    studentIdExists: "Student ID is already registered.",
                    resumeRequiredBeforeSave: "Please upload your resume before saving your profile.",
                    resumeDraftUploaded: "Resume draft uploaded.",
                    resumeDraftDiscarded: "Resume draft discarded.",
                    detailRetrieved: "Applicant detail retrieved.",
                    noAccess: "You do not have access to this applicant.",
                    photoNotFound: "Profile photo not found.",
                    resumeFileUnavailable: "Resume file is unavailable.",
                    taProfileRequired: "TA profile not found. Please complete your profile first.",
                    notFoundWithId: "Applicant not found.",
                    fileUploadFailed: "File upload failed.",
                    fileInvalidType: "Invalid file type.",
                    fileInvalidExtension: "Invalid file extension.",
                    fileTooLarge: "File size exceeds the allowed limit.",
                    experienceTooLong: "Related experience is too long.",
                    motivationTooLong: "Motivation is too long.",
                    addressTooLong: "Address is too long.",
                    addressPunctuation: "Address cannot contain only punctuation."
                },
                job: {
                    listRetrieved: "Job list retrieved.",
                    retrieved: "Job retrieved.",
                    notFound: "Job not found.",
                    onlyMoPost: "Only MO accounts can publish jobs.",
                    created: "Job created.",
                    updated: "Job updated.",
                    deleted: "Job deleted.",
                    updateOwnOnly: "You can only update your own jobs.",
                    deleteOwnOnly: "You can only delete your own jobs.",
                    deleteFailed: "Failed to delete job.",
                    statusRequired: "Status is required.",
                    idRequiredLower: "jobId is required.",
                    idInvalidChars: "jobId contains unsupported characters."
                },
                application: {
                    idRequiredLower: "applicationId is required.",
                    notFound: "Application not found.",
                    listRetrieved: "Application list retrieved.",
                    retrieved: "Application retrieved.",
                    submitted: "Application submitted.",
                    accepted: "Application accepted.",
                    rejected: "Application rejected.",
                    withdrawn: "Application withdrawn.",
                    actionRequired: "Action is required.",
                    jobNotFound: "Job not found for this application.",
                    onlyTaApply: "Only TA accounts can submit applications.",
                    alreadyApplied: "You have already applied for this job.",
                    alreadyReviewed: "This application has already been reviewed.",
                    withdrawFailed: "Failed to withdraw application.",
                    withdrawUnavailable: "This application cannot be withdrawn.",
                    reviewOwnJobsOnly: "You can only review applications for your own jobs."
                },
                workload: {
                    adminOnly: "Only admin accounts can view workload statistics.",
                    invalidStart: "Invalid work range start.",
                    invalidEnd: "Invalid work range end.",
                    startAfterEnd: "Start date cannot be after end date.",
                    onlyTaSupported: "Only TA workload statistics are supported.",
                    generated: "Workload report generated.",
                    jobRecordMissing: "Job record is missing.",
                    weeklyHoursMissing: "Weekly hours are missing.",
                    weeklyHoursDecimal: "Weekly hours must use at most one decimal place.",
                    startDateMissing: "Work start date is missing.",
                    endDateMissing: "Work end date is missing.",
                    endBeforeStart: "Work end date cannot be before work start date."
                },
                search: {
                    queryTooLong: "Search query is too long.",
                    queryInvalidChars: "Search query contains unsupported characters.",
                    ownJobsOnly: "You can only search applicants for your own jobs."
                },
                ai: {
                    moApplicantOnly: "Only MO accounts can use applicant AI search.",
                    taJobOnly: "Only TA accounts can use job AI search.",
                    applicantSearchUnavailable: "AI search is currently unavailable.",
                    jobSearchUnavailable: "AI recommendation is unavailable right now.",
                    profileRequired: "Please complete your profile before using AI recommendation.",
                    applicantRecommendationsGenerated: "AI recommendations generated.",
                    jobRecommendationsGenerated: "AI job recommendations generated."
                },
                notifications: {
                    titleRequired: "Title is required.",
                    contentRequired: "Content is required.",
                    notFound: "Notification not found."
                },
                common: {
                    endpointNotFound: "Endpoint not found.",
                    deleteUnsupported: "Delete is not supported.",
                    methodNotAllowed: "Method not allowed."
                }
            }
        },
        "zh-CN": {
            common: {
                portalBrand: "TA Hiring System",
                utility: {
                    backToPortal: "返回门户首页"
                },
                locale: {
                    switchAria: "切换语言",
                    zh: "中文",
                    en: "English"
                },
                action: {
                    signIn: "登录",
                    createAccount: "创建账号",
                    createAdmin: "创建管理员账号"
                },
                footer: {
                    copyright: "TA Hiring System © 2026"
                },
                password: {
                    show: "显示密码",
                    hide: "隐藏密码"
                }
            },
            index: {
                page: {
                    title: "TA Hiring System - 首页"
                },
                nav: {
                    aria: "主导航",
                    overview: "概览",
                    forTa: "面向 TA",
                    forMo: "面向 MO",
                    forAdmin: "面向管理员",
                    process: "流程",
                    faq: "常见问题"
                },
                hero: {
                    badge: "基于角色的 TA 招聘系统",
                    title: "协同管理 TA 档案、岗位、审核与工作量",
                    subtitle: "系统按 TA、MO 和 Admin 三类角色组织流程：TA 申请职位，MO 审核申请，Admin 管理工作量、邀请码和公告。",
                    primary: "开始使用",
                    secondary: "前往登录",
                    adminHint: "需要管理员权限？",
                    adminLink: "使用管理员邀请码"
                },
                preview: {
                    title: "当前项目模块概览",
                    subtitle: "首页描述的内容对应当前项目已经实现的角色页面和 API 流程。",
                    workflowAriaLabel: "门户流程预览",
                    jobKicker: "开放 TA 岗位",
                    jobMeta: "2 个名额 · 每周 8 小时",
                    reviewKicker: "MO 审核队列",
                    candidateStrong: "推荐申请人",
                    candidateReview: "人工复核",
                    adminKicker: "TA 工作量",
                    activeApplications: "已接受 TA 工作",
                    workflowTitle: "实时角色流程",
                    workflowSubtitle: "基于 CSV 的 Servlet/JSP 项目",
                    taLaneTitle: "申请人工作区",
                    taLaneMeta: "档案、职位、申请",
                    taItem1: "档案和简历已准备",
                    taItem2: "开放岗位可浏览",
                    taItem3: "申请状态可跟踪",
                    moLaneTitle: "课程负责人审核",
                    moLaneMeta: "岗位、申请人、决策",
                    moItem1: "已发布 TA 岗位",
                    moItem2: "按岗位查看申请人",
                    moItem3: "接受或拒绝申请",
                    adminLaneTitle: "管理员运营",
                    adminLaneMeta: "工作量、邀请码、公告",
                    adminItem1: "已接受 TA 工作量",
                    adminItem2: "8 位管理员邀请码",
                    adminItem3: "系统公告",
                    stateReady: "就绪",
                    stateOpen: "开放",
                    stateReview: "审核",
                    stateDecision: "决策",
                    stateActive: "进行中",
                    stateLive: "生效",
                    cardTaTitle: "TA 工作台",
                    cardTaDesc: "维护档案、上传简历/头像、浏览职位、提交申请，并跟踪申请状态。",
                    cardMoTitle: "MO 工作台",
                    cardMoDesc: "发布岗位、管理自己的岗位、审核申请，并接受或拒绝候选人。",
                    cardAdminTitle: "管理员工作台",
                    cardAdminDesc: "查看已接受的 TA 工作量、刷新邀请码，并发布公告。"
                },
                forTa: {
                    title: "面向助教申请人",
                    lead: "从档案准备到申请状态跟踪，TA 常用流程都可以在系统内完成。",
                    item1: "创建并维护个人档案、简历和技能信息。",
                    item2: "搜索开放岗位，也可以请求可选的 AI 职位推荐。",
                    item3: "提交申请、查看职位详情，并跟踪待处理、通过、拒绝或撤回等状态。",
                    cta: "以 TA 身份登录"
                },
                forMo: {
                    title: "面向课程负责人（MO）",
                    lead: "发布岗位、管理自己的岗位，并在同一流程中审核申请。",
                    item1: "创建和维护包含课程、技能、名额、工作量、薪酬和截止时间的岗位。",
                    item2: "从自己的岗位进入申请人列表，查看档案、简历和求职信。",
                    item3: "接受或拒绝申请，也可以把 AI 推荐与分析作为辅助参考。",
                    cta: "以 MO 身份登录"
                },
                forAdmin: {
                    title: "面向系统管理员",
                    lead: "管理支撑招聘流程运行的后台能力。",
                    item1: "按 TA、岗位、课程、每周工时和生效周期查看已接受的 TA 工作量。",
                    item2: "查看或刷新当前 8 位管理员邀请码。",
                    item3: "发布 TA、MO 和 Admin 都能阅读的系统公告。",
                    cta: "以管理员身份登录"
                },
                process: {
                    title: "从注册到最终录用的全流程",
                    lead: "首页展示的流程与系统当前实现的端到端能力一致。",
                    step1Title: "1. 注册账号",
                    step1Desc: "TA/MO 使用普通注册，管理员账号通过 8 位邀请码创建。",
                    step2Title: "2. 完善档案或发布职位",
                    step2Desc: "TA 完善个人资料，MO 发布带要求和截止时间的岗位。",
                    step3Title: "3. 申请与审核",
                    step3Desc: "TA 提交申请，MO 审核候选人并做出录用决策。",
                    step4Title: "4. 跟踪状态与工作量",
                    step4Desc: "TA 跟踪申请结果，MO 完成审核决策，Admin 查看已接受的 TA 工作量。"
                },
                ai: {
                    title: "可选的 AI 推荐与分析",
                    lead: "AI 是 TA 和 MO 流程中的可选辅助，不是单独的独立流程。",
                    item1: "TA 可以基于个人档案和开放岗位请求职位推荐。",
                    item2: "MO 可以针对自己发布的岗位请求申请人推荐。",
                    item3: "TA/MO 详情页可以请求分析；AI 不可用时，人工审核流程仍可继续。"
                },
                faq: {
                    title: "常见问题",
                    q1: "每次都必须先访问这个首页吗？",
                    a1: "不需要。老用户可以直接打开登录页继续使用。",
                    q2: "我应该选择哪个角色？",
                    a2: "申请人请选择 TA，课程负责人请选择 MO，只有拿到邀请码的平台管理人员才选择 Admin。",
                    q3: "之后还能切换语言吗？",
                    a3: "可以。右上角可随时切换语言，系统会记住你的选择。"
                },
                cta: {
                    title: "准备好开始 TA 招聘流程了吗？",
                    subtitle: "先通过门户了解全貌，再进入你需要的登录流程。",
                    primary: "立即登录",
                    secondary: "创建账号"
                }
            },
            login: {
                page: {
                    title: "登录 - TA 招聘系统"
                },
                hero: {
                    title: "TA Hiring System",
                    subtitle: "登录你的账号"
                },
                form: {
                    aria: "登录表单",
                    usernameLabel: "用户名或邮箱",
                    usernamePlaceholder: "用户名 或 name@university.edu",
                    passwordLabel: "密码",
                    passwordPlaceholder: "输入你的密码",
                    forgot: "忘记密码？",
                    keepSignedIn: "保持登录状态",
                    roleLabel: "登录角色",
                    roleAria: "角色选择按钮",
                    ta: "TA",
                    taDesc: "申请人",
                    mo: "MO",
                    moDesc: "课程负责人",
                    admin: "管理员",
                    adminDesc: "平台管理",
                    submit: "登录"
                },
                links: {
                    noAccount: "还没有账号？",
                    createAccount: "立即注册",
                    needAdmin: "需要管理员权限？",
                    createAdmin: "使用管理员邀请"
                },
                msg: {
                    failed: "登录失败，请检查用户名和密码。",
                    successRedirect: "登录成功，正在跳转...",
                    credentialError: "用户名或邮箱或密码输入有误。",
                    roleError: "角色选择出错。",
                    loggingIn: "登录中...",
                    enterIdentifier: "请输入用户名或邮箱。",
                    identifierTooLong: "用户名或邮箱过长。",
                    identifierUnsupported: "用户名或邮箱包含不支持的字符。",
                    invalidEmail: "请输入有效的邮箱地址。",
                    invalidUsername: "用户名需以字母开头，长度 3-20，仅允许字母、数字和下划线。",
                    enterPassword: "请输入密码。",
                    passwordTooShort: "密码长度至少为 8 位。",
                    passwordTooLong: "密码过长。",
                    passwordUnsupported: "密码包含不支持的字符。",
                    networkError: "网络异常，请稍后重试。"
                }
            },
            register: {
                page: {
                    title: "注册 - TA 招聘系统"
                },
                hero: {
                    title: "创建账号",
                    subtitle: "几步开始使用 TA Hiring System"
                },
                form: {
                    aria: "注册表单",
                    usernameLabel: "用户名",
                    usernamePlaceholder: "john_smith",
                    usernameHint: "长度 3-20，需以字母开头，仅允许字母、数字和下划线。",
                    usernameInfoAria: "用户名填写规则",
                    usernameTooltip: "长度 3-20，字母开头，仅允许字母、数字和下划线，不可有连续下划线或末尾下划线。",
                    emailLabel: "邮箱地址",
                    emailPlaceholder: "name@university.edu",
                    emailInfoAria: "邮箱填写规则",
                    emailTooltip: "请输入有效的邮箱地址（如 name@university.edu）。",
                    passwordLabel: "密码",
                    passwordPlaceholder: "创建一个密码",
                    passwordHint: "至少 8 个字符。",
                    passwordInfoAria: "密码填写规则",
                    passwordTooltip: "至少 8 个字符，且必须包含至少一个字母和一个数字。",
                    passwordTooSimple: "密码必须包含至少一个字母和一个数字。",
                    confirmLabel: "确认密码",
                    confirmPlaceholder: "再次输入密码",
                    confirmInfoAria: "确认密码填写规则",
                    confirmTooltip: "请再次输入上方设置的密码。",
                    roleLabel: "注册身份",
                    roleAria: "角色选择按钮",
                    roleTaTitle: "TA",
                    roleTaDesc: "申请人",
                    roleMoTitle: "MO",
                    roleMoDesc: "课程负责人",
                    submit: "创建账号"
                },
                links: {
                    haveAccount: "已有账号？",
                    backLogin: "返回登录",
                    adminQuestion: "需要管理员账号？",
                    adminLink: "使用管理员邀请"
                },
                msg: {
                    enterUsername: "请输入用户名。",
                    usernameTooLong: "用户名过长。",
                    usernameUnsupported: "用户名包含不支持的字符。",
                    usernameInvalid: "用户名需以字母开头，长度 3-20，仅允许字母、数字和下划线。",
                    usernameConsecutiveUnderscore: "用户名不能包含连续下划线。",
                    usernameTrailingUnderscore: "用户名不能以下划线结尾。",
                    usernameUnavailable: "该用户名已被注册。",
                    enterEmail: "请输入邮箱地址。",
                    emailTooLong: "邮箱过长。",
                    emailUnsupported: "邮箱包含不支持的字符。",
                    emailInvalid: "请输入有效的邮箱地址。",
                    emailUnavailable: "该邮箱已被注册。",
                    enterPassword: "请创建密码。",
                    passwordTooShort: "密码长度至少为 8 位。",
                    passwordTooLong: "密码过长。",
                    passwordUnsupported: "密码包含不支持的字符。",
                    passwordTooSimple: "密码必须包含至少一个字母和一个数字。",
                    enterConfirmPassword: "请确认密码。",
                    passwordMismatch: "两次输入的密码不一致。",
                    selectRole: "请选择角色。",
                    adminUsePage: "管理员账号需通过邀请码页面开通。",
                    failed: "注册失败，请检查信息后重试。",
                    successRedirect: "注册成功，正在跳转登录页...",
                    networkError: "网络异常，请稍后重试。"
                }
            },
            adminInvite: {
                page: {
                    title: "管理员邀请注册 - TA 招聘系统"
                },
                hero: {
                    title: "完成管理员邀请注册",
                    subtitle: "使用团队提供的邀请码创建管理员账号"
                },
                contactHint: {
                    intro: "如需获取邀请码，请用你将注册的邮箱向以下联系地址发送申请邮件，管理员将以邀请码回复。",
                    contactLabel: "联系邮箱：",
                    contactEmail: "admin@example.com"
                },
                form: {
                    aria: "管理员邀请注册表单",
                    emailLabel: "邮箱地址",
                    emailPlaceholder: "admin@university.edu",
                    emailInfoAria: "邮箱地址要求",
                    emailTooltip: "请输入你将用于注册的邮箱地址。",
                    inviteCodeLabel: "邀请码",
                    inviteCodePlaceholder: "ABCDEFGH",
                    inviteCodeInfoAria: "邀请码说明",
                    inviteCodeTooltip: "输入管理员提供的 8 位邀请码。",
                    usernameLabel: "用户名",
                    usernamePlaceholder: "admin_username",
                    usernameInfoAria: "用户名要求",
                    usernameTooltip: "长度 3-20，字母开头，仅允许字母、数字和下划线，不可有连续下划线或末尾下划线。",
                    passwordLabel: "密码",
                    passwordPlaceholder: "创建密码",
                    passwordInfoAria: "密码要求",
                    passwordTooltip: "至少 8 个字符，须包含至少一个字母和一个数字。",
                    confirmLabel: "确认密码",
                    confirmPlaceholder: "再次输入密码",
                    confirmInfoAria: "确认密码要求",
                    confirmTooltip: "再次输入密码以确认。",
                    submit: "创建管理员账号"
                },
                links: {
                    haveAccount: "已有账号？",
                    backLogin: "返回登录"
                },
                msg: {
                    passwordTooShort: "密码长度至少为 8 位。",
                    passwordTooLong: "密码过长。",
                    passwordMismatch: "两次输入的密码不一致。",
                    createFailed: "创建管理员账号失败。",
                    createSuccessRedirect: "管理员账号创建成功，正在跳转登录页...",
                    networkError: "网络异常，请稍后重试。",
                    creating: "创建中...",
                    enterEmail: "请输入邮箱地址。",
                    emailTooLong: "邮箱地址过长。",
                    emailUnsupported: "邮箱包含不支持的字符。",
                    emailInvalid: "请输入有效的邮箱地址。",
                    emailUnavailable: "该邮箱已被注册。",
                    enterUsername: "请输入用户名。",
                    usernameTooLong: "用户名过长。",
                    usernameUnsupported: "用户名包含不支持的字符。",
                    usernameInvalid: "用户名须以字母开头，长度 3-20，仅允许字母、数字和下划线。",
                    usernameConsecutiveUnderscore: "用户名不能包含连续的下划线。",
                    usernameTrailingUnderscore: "用户名不能以下划线结尾。",
                    usernameUnavailable: "该用户名已被注册。",
                    enterPassword: "请创建密码。",
                    passwordUnsupported: "密码包含不支持的字符。",
                    passwordTooSimple: "密码须包含至少一个字母和一个数字。",
                    enterConfirmPassword: "请再次输入密码。",
                    inviteCodeRequired: "邀请码不能为空。",
                    inviteCodeInvalidOrExpired: "邀请码无效或已过期。"
                }
            },
            portal: {
                action: {
                    signOut: "退出登录",
                    switchRoles: "切换角色",
                    save: "保存更改",
                    cancel: "取消",
                    edit: "编辑",
                    delete: "删除",
                    inbox: "收件箱"
                },
                accountProfile: {
                    open: "编辑账号展示资料",
                    kicker: "账号展示",
                    title: "编辑账号资料",
                    chooseAvatar: "选择头像",
                    avatarHintTa: "这个头像只用于账号展示。MO 审核申请时仍会看到你在 TA 档案中保存的头像。",
                    avatarHintMo: "这个头像只用于账号展示。职位卡片会使用下方头衔和真实姓名。",
                    nickname: "昵称",
                    realName: "真实姓名",
                    professionalTitle: "头衔",
                    professionalTitlePlaceholder: "例如 Dr. / Prof.",
                    saved: "账号资料已保存。",
                    saveFailed: "账号资料暂时无法保存。"
                },
                brand: {
                    ta: "TA 门户",
                    mo: "MO 门户",
                    admin: "管理员门户"
                },
                nav: {
                    ta: {
                        aria: "TA 门户导航",
                        jobs: "职位列表",
                        status: "我的申请",
                        profile: "个人档案",
                        notifications: "通知"
                    },
                    mo: {
                        aria: "MO 门户导航",
                        myJobs: "我的发布",
                        postJob: "发布新职位",
                        notifications: "通知"
                    },
                    admin: {
                        aria: "管理员门户导航",
                        dashboard: "TA工作量",
                        invite: "邀请码",
                        notifications: "通知"
                    }
                },
                page: {
                    taDashboard: {
                        title: "TA 档案设置 - TA 招聘系统"
                    },
                    taJobList: {
                        title: "职位列表 - TA 招聘系统"
                    },
                    taJobDetail: {
                        title: "职位详情 - TA 招聘系统"
                    },
                    taApplicationStatus: {
                        title: "申请状态 - TA 招聘系统"
                    },
                    taApplicationDetail: {
                        title: "申请详情 - TA 招聘系统"
                    },
                    moDashboard: {
                        title: "MO 仪表盘 - 发布 TA 职位"
                    },
                    adminDashboard: {
                        title: "TA工作量 - TA 招聘系统"
                    },
                    adminInviteManagement: {
                        title: "管理员邀请管理 - TA 招聘系统"
                    },
                    taNotifications: {
                        title: "通知 - TA 招聘系统"
                    },
                    moNotifications: {
                        title: "通知 - TA 招聘系统"
                    },
                    adminNotifications: {
                        title: "通知 - TA 招聘系统"
                    }
                },
                notifications: {
                    subtitle: "管理员发布的系统公告",
                    empty: "暂无通知",
                    composeTitle: "发布通知",
                    titleLabel: "标题",
                    titlePlaceholder: "通知标题",
                    contentLabel: "正文",
                    contentPlaceholder: "在此处输入通知内容…",
                    publishBtn: "发布",
                    deleteBtn: "删除",
                    publishedBy: "发布者",
                    published: "通知已发布。",
                    fillAll: "请填写标题和正文。",
                    deleteFailed: "删除通知失败。",
                    publishFailed: "发布通知失败。",
                    networkError: "网络异常。"
                },
                common: {
                    search: "搜索",
                    keyword: "关键词",
                    all: "全部",
                    open: "开放中",
                    closed: "已关闭",
                    filled: "已满额",
                    openUpper: "开放中",
                    courseCode: "课程编号",
                    applyFilters: "应用筛选",
                    clear: "清空",
                    refresh: "刷新",
                    close: "关闭",
                    positions: "名额",
                    workload: "工作量",
                    salary: "薪资",
                    deadline: "截止时间",
                    description: "描述",
                    requiredSkills: "所需技能",
                    application: "申请",
                    pending: "待处理",
                    accepted: "已通过",
                    rejected: "已拒绝",
                    withdrawn: "已撤回",
                    total: "总数",
                    selectJob: "选择职位",
                    high: "高",
                    medium: "中",
                    low: "低",
                    job: "职位",
                    course: "课程",
                    status: "状态",
                    processed: "已处理",
                    loading: "加载中..."
                },
                taDashboard: {
                    subtitle: "管理你的个人信息与学术背景。",
                    profileLayoutAria: "TA 档案表单和已保存档案",
                    createProfileTitle: "创建你的 TA 档案",
                    basicDetails: "基础信息",
                    fullName: "姓名",
                    fullNameInfoAria: "姓名格式说明",
                    fullNameTooltip: "字母、空格、连字符、撇号和句点，至少 2 个字符。",
                    fullNamePlaceholder: "你的全名",
                    required: "必填",
                    studentId: "学号",
                    studentIdInfoAria: "学号格式说明",
                    studentIdTooltip: "10位数字，以20开头，例如 2023213039。",
                    studentIdPlaceholder: "例如 2023213039",
                    department: "院系",
                    departmentInfoAria: "院系格式说明",
                    departmentTooltip: "你所在的院系名称，2–100 个字符。",
                    departmentPlaceholder: "院系名称",
                    program: "学位项目",
                    programInfoAria: "学位项目说明",
                    programTooltip: "选择与你当前学籍相符的学位层次。",
                    selectProgram: "选择你的学位项目",
                    programUndergraduate: "本科",
                    programMaster: "硕士",
                    programPhd: "博士",
                    additionalInfo: "补充信息",
                    additionalInfoLead: "这些字段当前为可选，但完善后会让你的档案更完整。",
                    gpa: "绩点",
                    gpaInfoAria: "绩点格式说明",
                    gpaTooltip: "填写你的绩点，例如 3.85 或 3.85/4.00（成绩/满分）。",
                    gpaPlaceholder: "例如 3.85 / 4.00",
                    phone: "手机号",
                    phoneInfoAria: "手机号格式说明",
                    phoneTooltip: "8–15位数字，支持国际格式，例如 +86 138 0000 0000。",
                    phonePlaceholder: "+86 138 0000 0000",
                    skills: "技能",
                    skillsInfoAria: "技能填写说明",
                    skillsTooltip: "用英文逗号或中文逗号分隔，最多12项，例如 Java, SQL, Python。",
                    skillsPlaceholder: "用逗号分隔技能，例如 Java, JSP, SQL",
                    skillsHint: "请使用英文逗号或中文逗号分隔每项技能，系统会按列表存储。",
                    experience: "相关经历",
                    experienceInfoAria: "相关经历说明",
                    experienceTooltip: "描述相关的教学或辅导经历，至少 10 个词/字。",
                    experiencePlaceholder: "描述与 TA 职责相关的辅导、教学、批改或项目经历。",
                    motivation: "申请动机",
                    motivationInfoAria: "申请动机说明",
                    motivationTooltip: "说明你申请本 TA 职位的动机，至少 10 个词/字。",
                    motivationPlaceholder: "说明你申请本 TA 职位的原因及你能带来的价值。",
                    createProfileButton: "创建档案",
                    saveChangesButton: "保存更改",
                    editProfileButton: "编辑档案",
                    cancelButton: "取消",
                    profileLabel: "个人档案",
                    profilePhotoAlt: "个人头像",
                    photoUploadTitle: "照片上传",
                    photoCardEmptyTitle: "上传你的照片",
                    photoCardEmptyHint: "支持 JPG、PNG 或 WEBP，最大 5MB。",
                    photoRemoveAria: "删除照片",
                    resumeUploadTitle: "简历上传",
                    resumeUploadLead: "请上传一份 PDF、DOC 或 DOCX 简历，最大 10MB。",
                    resumeCardEmptyTitle: "上传您的简历",
                    resumeCardEmptyHint: "PDF、DOC 或 DOCX，最大 10MB",
                    chooseFile: "选择文件",
                    resumeRemoveAria: "删除简历",
                    noFileSelected: "尚未选择文件。",
                    waitingUpload: "等待上传",
                    resumeDraftTitle: "统一保存",
                    createProfileFirst: "请先创建档案",
                    resumeTip: "你可以先上传简历，也可以先填写其他字段；最新简历会在点击保存更改后正式生效。",
                    uploadSelectedResume: "上传已选简历",
                    validation: {
                        fullName: {
                            required: "请输入你的姓名。",
                            tooLong: "姓名不得超过100个字符。",
                            tooShort: "姓名至少需要2个字符。",
                            noLetter: "姓名必须包含至少一个字母。",
                            invalidChars: "姓名只能包含字母、空格、撇号、句点和连字符。",
                            tooManyRepeated: "姓名中重复字符过多。"
                        },
                        studentId: {
                            required: "请输入你的学号。",
                            notTenDigits: "学号必须是恰好10位数字，例如 2023213039。",
                            notStartWith20: "学号应以20开头，例如 2023213051。",
                            invalidYear: "学号年份看起来无效，请检查前四位数字。",
                            allSameDigit: "学号看起来无效，请检查你的官方10位学号。"
                        },
                        department: {
                            required: "请输入你的院系。",
                            tooLong: "院系名称不得超过100个字符。",
                            tooShort: "院系名称至少需要2个字符。",
                            noLetter: "院系名称应包含字母。",
                            invalidChars: "院系名称包含不支持的字符。",
                            tooManyRepeated: "院系名称中重复字符过多。"
                        },
                        program: {
                            required: "请选择你的学位项目。",
                            invalidOption: "请选择一个有效的学位项目。"
                        },
                        gpa: {
                            required: "请输入你的绩点。",
                            tooLong: "绩点不得超过20个字符。",
                            invalidChars: "绩点只能包含数字、空格、小数点和"/"。",
                            multipleSlash: "绩点格式无效，最多使用一个"/"。",
                            invalidValue: "绩点值最多支持2位小数。",
                            negative: "绩点不能为负数。",
                            invalidScale: "绩点满分最多支持2位小数。",
                            scaleOutOfRange: "绩点满分应在4到100之间。",
                            valueExceedsScale: "绩点值不能大于绩点满分。",
                            tooHighWithoutScale: "绩点高于4.3时，请注明满分，例如 85/100。"
                        },
                        skills: {
                            required: "请至少输入一项技能。",
                            tooLong: "技能不得超过300个字符。",
                            emptyItems: "请删除逗号之间的空白技能项。",
                            useCommaSeparator: "请使用英文逗号或中文逗号分隔技能。",
                            tooManySkills: "最多列出12项技能。",
                            skillLength: "每项技能应在2到40个字符之间。",
                            noLetter: "每项技能应包含字母。",
                            invalidChars: "技能中包含不支持的字符。",
                            tooManyRepeated: "某项技能中重复字符过多。",
                            duplicate: "存在重复技能，请保留每项技能一次。"
                        },
                        phone: {
                            required: "请输入你的手机号。",
                            tooLong: "手机号不得超过30个字符。",
                            invalidChars: "手机号只能包含数字、空格及 + - ( ) . /。",
                            multiplePlus: "手机号中最多只能有一个"+"。",
                            plusNotAtStart: "如使用"+"，必须放在最前面。",
                            unbalancedParens: "手机号中的括号不匹配。",
                            digitCount: "手机号应包含8到15位数字。",
                            allSameDigit: "手机号看起来无效，请检查重复数字。",
                            internationalTooShort: "国际格式的手机号通常至少应有10位数字。"
                        },
                        experience: {
                            required: "请描述你的相关经历。",
                            tooLong: "相关经历不得超过1200个字符。",
                            tooShort: "相关经历如有填写，应至少20个字符。",
                            notEnoughDetail: "相关经历请提供更多详细内容（约10个词/字）。",
                            tooManyRepeated: "相关经历中重复字符过多。"
                        },
                        motivation: {
                            required: "请说明你的申请动机。",
                            tooLong: "申请动机不得超过1200个字符。",
                            tooShort: "申请动机如有填写，应至少20个字符。",
                            notEnoughDetail: "申请动机请提供更多详细内容（约10个词/字）。",
                            tooManyRepeated: "申请动机中重复字符过多。"
                        }
                    }
                },
                taJobList: {
                    subtitle: "浏览并申请当前开放的 TA 职位。",
                    panelAria: "TA 职位搜索与结果列表",
                    loadingPositions: "正在加载职位...",
                    searchPlaceholder: "按职位名称、课程编号或关键词搜索",
                    aiSearchPlaceholder: "输入你的需求，让 AI 基于个人档案推荐职位",
                    aiSearchButton: "AI",
                    aiSearching: "AI 搜索中...",
                    aiSearchLoading: "AI 正在推荐职位...",
                    aiSearchUnavailable: "AI 推荐暂不可用，请稍后再试。",
                    aiOutOfScope: "我无法处理您的问题。我可以根据你的个人档案和当前开放职位，帮你推荐职位、比较职位或解释推荐理由。",
                    aiRecommendationTitle: "推荐建议（AI生成）",
                    aiRecommendedUnit: "AI 推荐",
                    aiNoRecommendations: "当前开放职位暂无 AI 推荐结果。",
                    aiNoRecommendationsHint: "可以换一个教学重点再试，或等有更多开放职位后再查看。",
                    searchModeToggle: "搜索模式"
                },
                taJobDetail: {
                    title: "职位详情",
                    subtitle: "查看岗位要求并提交你的申请。",
                    detailCardAria: "职位详情卡片",
                    backToJobs: "← 返回职位列表",
                    loadingDetails: "正在加载职位详情...",
                    missingId: "缺少职位编号，请返回列表后重试。",
                    moduleOrganizer: "课程负责人",
                    submitApplicationTitle: "提交你的申请",
                    applyProfileHint: "点击提交后，个人档案将和求职信一起发送给 MO。",
                    coverLetterHint: "可补充一段简短的求职信来说明你的匹配度。",
                    coverLetterPlaceholder: "简要说明你的相关经历、优势以及可投入时间。",
                    coverLetter: "求职信",
                    applyNow: "申请该职位",
                    onlyTaHint: "仅 TA 账号可提交申请。若你已申请，本面板将显示最新状态。"
                },
                taApplicationStatus: {
                    title: "我的申请",
                    subtitle: "跟踪你已提交申请的状态变化。",
                    panelAria: "申请搜索与状态列表",
                    loadingApplications: "正在加载申请...",
                    searchPlaceholder: "按职位名称、课程编号或 MO 搜索"
                },
                taApplicationDetail: {
                    title: "申请详情",
                    backToList: "← 我的申请",
                    jobTeaserTitle: "申请职位详情",
                    viewDetailsCta: "查看详情 →",
                    gpaScale: "满分 4.0",
                    progressTitle: "申请进度",
                    mySkills: "我的技能",
                    responsibilities: "职责描述",
                    viewResumeFile: "查看文件",
                    resumeShort: "简历",
                    closeModal: "关闭",
                    missingId: "缺少申请编号，请返回列表重试。",
                    loadAppFailed: "无法加载申请信息。",
                    networkError: "网络异常，请稍后重试。",
                    untitled: "未命名职位",
                    submittedPrefix: "提交于",
                    noSkills: "暂无技能信息",
                    noCoverLetter: "未提供求职信。",
                    jobUnavailable: "暂时无法加载职位详情。",
                    workload: "工作时长",
                    applicants: "申请人数",
                    deadline: "申请截止",
                    deadlinePrefix: "截至",
                    noDescription: "暂无职位描述。",
                    sessionExpired: "登录已过期，正在跳转登录页...",
                    withdrawAction: "撤回申请",
                    withdrawing: "撤回中...",
                    withdrawConfirm: "确定撤回这条申请吗？MO 将看到该申请为已撤回。",
                    withdrawSuccess: "申请撤回成功。",
                    withdrawFailed: "无法撤回该申请。",
                    withdrawUnavailable: "该申请无法撤回。",
                    withdrawNetworkError: "撤回申请时网络异常。",
                    progressSubmit: "提交申请",
                    progressReview: "材料审核中",
                    progressInterview: "面试安排",
                    progressFinal: "最终决定",
                    profileCardTitle: "我的个人档案",
                    profileCardHint: "查看或编辑您的简历与技能信息。",
                    profileCardHintReady: "查看或编辑您的简历与技能信息。",
                    profileCardHintMissingResume: "请前往个人档案补充或更新简历、技能和基础信息。",
                    profileSyncNote: "您的档案简历已随求职信一同发送给当前职位 MO，如需更新信息请前往个人档案修改，更新后会自动同步到 MO 界面。",
                    profileSyncUpdatedPrefix: "最近同步时间："
                },
                moDashboard: {
                    title: "发布新职位",
                    subtitle: "为你的课程创建新的 TA 招聘职位。",
                    myJobsHeroSubtitle: "查看并管理你已经发布的 TA 职位。",
                    myJobsPanelAria: "我发布的职位",
                    postJobPanelAria: "发布新职位表单",
                    createPosting: "创建职位",
                    manage: "管理",
                    postPosition: "发布新的 TA 职位",
                    jobTitle: "职位名称",
                    jobTitlePlaceholder: "例如：数据结构课程助教",
                    courseCodePlaceholder: "例如：EBU6304",
                    courseNamePlaceholder: "例如：软件工程",
                    descriptionPlaceholder: "描述职责、期望以及课程相关要求。",
                    requiredSkillsPlaceholder: "仅可用英文逗号或中文逗号分隔，例如：Java, SQL, 沟通能力",
                    workloadPlaceholder: "例如：每周 8 小时",
                    weeklyHoursPlaceholder: "例如：8",
                    salaryPlaceholder: "例如：每小时 25 RMB",
                    required: "必填",
                    requiredLead: "标记“必填”的字段为发布必填项。",
                    hintAria: "填写提示",
                    hint: {
                        title: "最多 200 字符，不含 HTML 标签。",
                        courseCode: "字母或数字开头，如 EBU6304，最多 50 字符，不含空格。",
                        courseName: "课程全称，最多 120 字符。",
                        description: "详细描述职责与要求，最多 4000 字符。",
                        requiredSkills: "必须用英文逗号或中文逗号分隔，最多 20 项，如：Java, SQL, Git。",
                        positions: "招募名额，1 至 200 之间的整数。",
                        deadline: "须晚于当前时间，且不超过 2 年。",
                        weeklyHours: "每周工作小时数，0.5 至 40，最多 1 位小数。",
                        workStartDate: "不得早于申请截止日期。",
                        workEndDate: "不得早于工作开始日期。",
                        salary: "自由描述，如：25 RMB / 小时，最多 120 字符。"
                    },
                    courseInfo: "课程信息",
                    roleRequirements: "岗位要求",
                    hiringSettings: "招聘设置",
                    jobTitleRequired: "职位名称 *",
                    courseCodeRequired: "课程编号 *",
                    courseName: "课程名称",
                    applicationDeadline: "申请截止时间",
                    weeklyHours: "每周小时数",
                    workStartDate: "工作开始日期",
                    workEndDate: "工作结束日期",
                    publishJob: "发布职位",
                    publishing: "发布中...",
                    resetForm: "重置表单",
                    myPostings: "我的发布",
                    myJobs: "我的发布",
                    postNew: "发布新职位",
                    myJobsDesc: "查看并管理你发布的职位。",
                    publishedJobs: "已发布职位",
                    loadingJobs: "正在加载你的职位...",
                    noJobsTitle: "暂无职位发布",
                    noJobsDesc: "点击“发布新职位”创建第一个 TA 职位。",
                    editJob: "编辑职位",
                    confirmDelete: "确认删除",
                    deleteConfirmMsg: "确定要删除这条职位发布吗？",
                    userNotLoggedIn: "用户尚未登录。",
                    failedLoadJobs: "加载职位失败。",
                    jobNotFound: "未找到职位。",
                    failedUpdateJob: "更新职位失败。",
                    jobUpdatedSuccess: "职位更新成功。",
                    failedDeleteJob: "删除职位失败。",
                    jobDeletedSuccess: "职位删除成功。",
                    deleting: "删除中...",
                    noDeadline: "无截止时间",
                    validationJobTitleRequired: "请填写职位名称。",
                    validationJobTitleLength: "职位名称不能超过 200 个字符。",
                    validationJobTitleUnsupported: "职位名称包含不支持的字符。",
                    validationCourseCodeRequired: "请填写课程编号。",
                    validationCourseCodeLength: "课程编号不能超过 50 个字符。",
                    validationCourseCodeUnsupported: "课程编号包含不支持的字符。",
                    validationCourseNameRequired: "请填写课程名称。",
                    validationCourseNameLength: "课程名称不能超过 120 个字符。",
                    validationCourseNameUnsupported: "课程名称包含不支持的字符。",
                    validationDescriptionRequired: "请填写职位描述。",
                    validationDescriptionLength: "职位描述不能超过 4000 个字符。",
                    validationDescriptionUnsupported: "职位描述包含不支持的字符。",
                    validationSkillsRequired: "请填写所需技能。",
                    validationSkillsLength: "所需技能不能超过 500 个字符。",
                    validationSkillsUnsupported: "所需技能包含不支持的字符。",
                    validationSkillsEmpty: "请移除空的技能项。",
                    validationSkillsCommaSeparator: "请使用英文逗号或中文逗号分隔技能。",
                    validationSkillsDuplicate: "存在重复技能，请每项技能只保留一次。",
                    validationSkillsLimit: "最多填写 20 个技能。",
                    validationPositionsRequired: "名额必须是整数。",
                    validationPositionsNumber: "名额必须是整数。",
                    validationPositionsRange: "名额必须在 1 到 200 之间。",
                    validationWorkloadRequired: "请填写工作量。",
                    validationWorkloadLength: "工作量不能超过 120 个字符。",
                    validationWorkloadUnsupported: "工作量包含不支持的字符。",
                    validationWeeklyHoursRequired: "请填写每周小时数。",
                    validationWeeklyHoursNumber: "每周小时数必须是数字，且最多 1 位小数。",
                    validationWeeklyHoursRange: "每周小时数必须在 0.5 到 40 之间。",
                    validationWorkDateRequired: "请填写{field}。",
                    validationWorkDateInvalid: "{field}格式必须为 yyyy-MM-dd。",
                    validationWorkStartBeforeDeadline: "工作开始日期不能早于申请截止日期。",
                    validationWorkPeriodOrder: "工作结束日期不能早于工作开始日期。",
                    validationSalaryRequired: "请填写薪资。",
                    validationSalaryLength: "薪资不能超过 120 个字符。",
                    validationSalaryUnsupported: "薪资包含不支持的字符。",
                    validationDeadlineRequired: "请填写申请截止时间。",
                    validationDeadlineInvalid: "截止时间格式无效。",
                    validationDeadlinePast: "截止时间不能早于当前时间。",
                    validationDeadlineTooFar: "截止时间不能超过未来 2 年。",
                    backToMyJobs: "← 我的发布",
                    backToApplicants: "← 申请人列表"
                },
                moApplicantSelection: {
                    subtitle: "审核并管理所有候选人的申请。",
                    panelAria: "申请人筛选面板",
                    detailPanelAria: "申请人详情面板",
                    loadingApplications: "正在加载申请...",
                    searchPlaceholder: "按申请人姓名、邮箱或职位名称搜索",
                    aiSearchButton: "AI",
                    aiSearching: "AI 搜索中...",
                    aiSearchLoading: "AI 搜索中...",
                    aiSearchUnavailable: "AI 搜索暂不可用，请稍后再试。",
                    aiOutOfScope: "我无法处理您的问题。我可以根据当前职位的申请人信息，帮你推荐候选人、比较申请人或解释推荐理由。",
                    aiRecommendationTitle: "推荐建议（AI生成）",
                    aiRecommendedUnit: "条 AI 推荐",
                    searchModeToggle: "切换搜索模式",
                    job: "职位",
                    allJobs: "全部职位",
                    applicantProfile: "申请人档案",
                    selectApplicant: "选择申请人",
                    viewResume: "查看简历",
                    academic: "学术信息",
                    studentId: "学号",
                    department: "院系",
                    program: "学位项目",
                    gpa: "绩点",
                    contact: "联系方式",
                    email: "邮箱",
                    phone: "电话",
                    address: "地址",
                    application: "申请信息",
                    skills: "技能",
                    experience: "经历",
                    coverLetter: "求职信",
                    profileSyncedAt: "档案同步时间",
                    profileSync: "档案同步",
                    profileSyncHint: "申请人保存档案后会自动同步到此界面。",
                    backToCourseList: "返回课程列表",
                    backToApplicantList: "返回申请人列表",
                    courseApplicants: "申请人列表",
                    applicationDetail: "申请详情",
                    applicationDetailLead: "查看申请人档案并完成审核决策。",
                    openCourseApplicants: "查看该课程申请人",
                    courseOwnerFallback: "课程负责人暂不可用",
                    applicantsLabel: "申请人数",
                    jobCountLabel: "职位数",
                    noJobTitle: "暂无职位标题",
                    unknownApplicant: "未知申请人",
                    openApplicationDetail: "查看申请详情",
                    appliedAtLabel: "申请时间",
                    progressLabel: "进度",
                    unknownCourseCode: "未标记课程",
                    courseUnit: "课程",
                    courseOverviewMoLabel: "课程负责人",
                    courseOverviewRolesLabel: "在招 TA 岗位",
                    courseOverviewTaPostingsLabel: "TA 职位条数",
                    courseOverviewTaPostingsTitle: "本课程下已发布的 TA 招聘职位数量（按职位条数计，非申请人数）。",
                    courseOverviewApplicationsLabel: "申请总数",
                    courseOverviewApplicationsTitle: "本课程所有 TA 职位收到的申请合计。",
                    courseOverviewAcceptedTitle: "本课程中已被标记为「已通过」的申请数量。",
                    courseOverviewRejectedTitle: "本课程中已被标记为「已拒绝」的申请数量。",
                    courseOverviewWorkloadTitle: "职位上标注的每周工作量（例如每周学时）。",
                    courseOverviewDeadlineTitle: "本课程下各 TA 职位中最早的申请截止时间。",
                    courseOverviewNoDeadline: "未设置",
                    courseOverviewDescriptionHeading: "职责描述",
                    courseOverviewNoDescription: "暂无职位描述。",
                    courseOverviewSkillsHeading: "技能要求",
                    courseOverviewNoSkills: "未列出所需技能。",
                    hireChipVisual: "录用",
                    rejectChipVisual: "拒绝",
                    applicationMaterials: "申请材料",
                    resumeDocument: "简历",
                    resumeFormatHint: "已上传文件",
                    viewAction: "查看",
                    hireApplicant: "录用申请人",
                    rejectApplicant: "拒绝",
                    processing: "处理中...",
                    alreadyReviewed: "该申请已审核完毕。",
                    noCoverLetter: "未填写求职信。",
                    profileUnavailable: "申请人档案暂时无法加载。",
                    motivationLabel: "申请动机",
                    noSkillsListed: "未填写技能",
                    noExperience: "未填写相关经历。",
                    noMotivation: "未填写申请动机。",
                    resumeNotUploaded: "未上传简历",
                    ariaLabel: "申请人列表"
                },
                adminDashboard: {
                    title: "TA工作量",
                    subtitle: "按每周小时数和实际工作期统计已录用 TA 的工作量。",
                    panelAria: "TA 工作量页面",
                    start: "工作期开始",
                    end: "工作期结束",
                    searchLabel: "搜索",
                    searchPlaceholder: "按 TA 姓名、职位名称或课程编号搜索",
                    applyRange: "应用区间",
                    exportCsv: "导出 CSV",
                    inviteTitle: "邀请码",
                    inviteLead: "将当前邀请码通过邮件回复给申请人。",
                    codePanel: {
                        title: "当前邀请码",
                        subtitle: "复制此码并回复给申请人的邮件。",
                        displayAria: "当前管理员邀请码",
                        loading: "正在加载...",
                        refreshBtn: "立即刷新",
                        loadError: "邀请码加载失败。",
                        refreshError: "刷新邀请码失败。",
                        workflowTitle: "工作流程",
                        step1: "申请人用将注册的邮箱向团队联系邮箱发送申请邮件。",
                        step2: "在此页面查看当前邀请码。",
                        step3: "回复申请人邮件并附上邀请码。",
                        step4: "申请人在注册页面输入邀请码以创建管理员账号。"
                    },
                    taCount: "TA 人数",
                    acceptedJobs: "已通过职位",
                    totalWeeklyHours: "每周小时数",
                    totalWorkWeeks: "工作周数",
                    totalWorkHours: "总工时",
                    invalidJobs: "无效职位",
                    taWorkloadOverview: "TA 工作量排行",
                    taWorkloadLead: "按所选工作期内的已录用工时统计。",
                    taWorkload: "TA 工作量明细",
                    includedWorkloadPanel: "纳入统计的工作量",
                    includedWorkloadPanelAria: "纳入统计的 TA 工作量卡片",
                    includedWorkloadLead: "TA 卡片按已录用总工时从高到低排序，点击卡片查看明细。",
                    paginationAria: "TA 工作量分页",
                    pageButtonAria: "页码",
                    filteredSummary: "筛选自",
                    pageSummaryPrefix: "当前显示",
                    noWorkloadMatches: "没有匹配关键词的 TA 工作量。",
                    noWorkloadMatchesHint: "请换一个 TA 姓名、职位名称或课程编号再试。",
                    viewDetails: "查看明细",
                    collapseDetails: "收起明细",
                    invalidJobPanel: "未纳入统计的职位",
                    invalidJobPanelAria: "未纳入工作量统计的职位",
                    invalidJobLead: "这里列出已通过但无法计算工时的职位，例如缺少每周小时数、工作开始/结束日期，或字段格式无效。",
                    noInvalidJobs: "没有被排除的已通过职位。",
                    noInvalidJobsTitle: "为什么会有未纳入统计的职位",
                    noInvalidJobsHint: "这里保留为数据校验提示区。已通过职位如果缺少每周小时数、工作开始/结束日期，或字段格式无效，就会出现在这里。",
                    hoursPerWeek: "小时/周",
                    weeks: "周",
                    hours: "小时",
                    loadingWorkload: "正在加载工作量..."
                },
                dynamic: {
                    checkingProfile: "正在检查档案...",
                    savingChanges: "正在保存更改...",
                    profileAlreadyExists: "该账号已存在档案，正在加载已保存信息...",
                    fixHighlightedFields: "请先修正高亮字段后再试。",
                    noProfileFound: "暂未找到档案，请填写下方表单。",
                    unableCheckProfile: "暂时无法检查已有档案，你仍可尝试创建新档案。",
                    unableCreateProfile: "无法创建档案，请检查表单后重试。",
                    unableUpdateProfile: "无法更新档案，请检查表单后重试。",
                    profileCreatedUploadingResume: "档案创建成功，正在上传你选择的简历...",
                    profileCreatedResumeFailed: "档案已创建，但简历上传失败，请稍后重试。",
                    profileCreatedSuccess: "档案创建成功，已在下方显示保存信息。",
                    profileUpdatedSuccess: "档案更新成功。",
                    profileReadonly: "你的档案已创建，当前以只读模式显示。",
                    currentResumePrefix: "当前已上传简历：",
                    noResumeUploaded: "尚未上传简历。",
                    noResumeSelected: "尚未选择简历文件。",
                    chooseResumeFirst: "请先选择简历文件。",
                    resumeRequiredToSave: "请先上传简历，再保存档案。",
                    resumeDraftUploading: "正在上传简历草稿：",
                    resumeDraftSaved: "简历草稿已上传，点击保存更改后生效。",
                    resumeDraftReplaceSaved: "新简历已上传，点击保存更改后会替换当前简历。",
                    pendingResumeRemoved: "待保存简历已删除。",
                    savedResumeRemoved: "当前简历已移除，请先上传新的简历再保存更改。",
                    resumeReady: "简历已就绪",
                    choosePhotoFirst: "请先选择照片文件。",
                    photoReadyToSave: "照片已选择，点击保存更改后生效。",
                    savedPhotoRemoved: "当前照片已移除，点击保存更改后生效。",
                    photoReady: "照片已就绪",
                    pendingResumePrefix: "待保存简历：",
                    pendingResumeCreateSuffix: " 填写其他信息后点击保存更改即可创建档案。",
                    pendingResumeReplaceSuffix: " 点击保存更改后将替换当前简历。",
                    resumeDiscardFailed: "暂时无法丢弃待保存简历，请稍后重试。",
                    createProfileThenUpload: "请先创建档案，再上传简历。",
                    createProfileAutoUpload: "请先创建档案。创建成功后将自动上传已选简历。",
                    resumeWillUploadAfterCreate: "将于创建档案后上传",
                    resumeReadyAfterCreate: "简历文件已就绪，将在档案创建后自动上传。",
                    replaceUploadedResume: "替换已上传简历",
                    resumeReadyReplace: "简历文件已就绪，点击上传即可替换当前简历。",
                    uploading: "正在上传",
                    uploadCompleted: "上传完成",
                    uploadAborted: "上传已取消。",
                    uploadInterrupted: "上传中断，请重试。",
                    uploadNetworkError: "上传简历时网络异常，请重试。",
                    resumeUploadSuccess: "简历上传成功。",
                    resumeUpdateSuccess: "简历更新成功。",
                    resumeUploadFailed: "简历上传失败，请重试。",
                    invalidResumeFormat: "文件格式不支持，请上传 PDF、DOC 或 DOCX。",
                    resumeTooLarge: "文件超过 10MB，请选择更小的文件。",
                    invalidPhotoFormat: "照片格式不支持，请上传 JPG、PNG 或 WEBP。",
                    photoTooLarge: "照片超过 5MB，请选择更小的文件。",
                    resumeWaitingUpload: "等待上传",
                    noSpecificSkills: "未列出具体技能。",
                    searching: "搜索中...",
                    unableLoadJobs: "暂时无法加载职位。",
                    unableLoadJobsRetry: "暂时无法加载职位，请稍后重试。",
                    noJobsForFilters: "当前筛选条件下未找到职位。",
                    noJobsForSearch: "没有匹配关键词的职位。",
                    noJobsAvailable: "当前暂无可申请职位。",
                    showing: "显示",
                    jobUnit: "个职位",
                    unableLoadPositionsTitle: "无法加载职位",
                    refreshAfterNetworkCheck: "请检查网络后刷新列表重试。",
                    noPositionsPublishedTitle: "暂无已发布职位",
                    positionsAppearAfterPublish: "MO 发布新职位后会显示在这里。",
                    noMatchingPositionsTitle: "没有匹配的职位",
                    closestMatchesNotice: "未找到精确匹配，已显示最接近结果。",
                    tryAnotherKeyword: "换个关键词试试。",
                    broadenKeywordHint: "可尝试放宽关键词或清除部分筛选。",
                    noExtraTags: "暂无额外标签",
                    viewDetails: "查看详情",
                    applyNow: "立即申请",
                    moShort: "MO",
                    submitting: "提交中...",
                    jobIdMissing: "缺少职位编号。",
                    applicationSubmitted: "申请已提交。",
                    applicationSubmittedRedirect: "申请提交成功，正在跳转到申请状态页...",
                    failedSubmitApplication: "提交申请失败，请稍后重试。",
                    unableLoadJobDetailsNow: "当前无法加载职位详情。",
                    currentAccountCannotSubmit: "当前账号无法在此页面提交申请。",
                    onlyTaSubmit: "仅 TA 账号可提交申请。",
                    alreadyApplied: "你已经申请过该职位。",
                    jobNoLongerAvailable: "该职位不存在或已下线。",
                    jobNotAccepting: "该职位当前不接受新申请。",
                    positionCurrently: "该职位当前状态为",
                    newApplicationsDisabled: "。已关闭新申请。",
                    jobNotFound: "未找到该职位，可能已被移除。",
                    applicationUnavailable: "申请不可用",
                    applicationStatusPrefix: "申请状态：",
                    applicationAlreadySubmitted: "已提交过申请。",
                    applicationStopped: "已停止申请",
                    cannotSubmitMissingJobId: "由于缺少职位编号，无法提交申请。",
                    coverLetterControlChars: "求职信包含不支持的控制字符。",
                    coverLetterUnsupportedMarkup: "求职信包含不支持的标记内容。",
                    coverLetterTooLong: "求职信长度不能超过 2000 个字符。",
                    networkErrorSubmitApplication: "提交申请时网络异常。",
                    taOnlyPage: "该页面仅 TA 账号可访问。",
                    unableLoadApplications: "无法加载你的申请记录。",
                    unableLoadApplicationsNow: "当前无法加载申请列表。",
                    noApplicationsSubmitted: "你还未提交任何申请。",
                    noApplicationsForSearch: "没有匹配关键词的申请。",
                    noApplicationsForPostedJobs: "你的职位暂未收到申请。",
                    noApplicationsForPostedJobsHint: "当 TA 开始申请你发布的职位后，申请卡片会显示在这里。",
                    noApplicationsMatchFilters: "当前筛选条件下无匹配申请。",
                    applicationUnit: "个申请",
                    unableLoadApplicationsTitle: "无法加载申请",
                    noMatchingApplicationsTitle: "没有匹配的申请",
                    noApplicationsYetTitle: "暂无申请",
                    applicationTimeline: "申请进度",
                    applied: "已提交",
                    review: "审核中",
                    decision: "结果",
                    statusAppearsAfterApply: "提交职位申请后，状态会显示在这里。",
                    clearFiltersToBroaden: "可尝试清除状态或关键词筛选以扩大结果。",
                    applicationWithdrawnSuccess: "申请撤回成功。",
                    unableWithdrawApplication: "无法撤回该申请。",
                    networkErrorWithdrawApplication: "撤回申请时网络异常。",
                    appliedAt: "申请时间",
                    coverLetterColon: "求职信：",
                    noCoverLetterProvided: "未提供求职信。",
                    viewJob: "查看职位",
                    withdraw: "撤回",
                    onlyMoPublish: "仅 MO 账号可发布职位。",
                    validationDeadlineTooFar: "截止时间不能超过未来 2 年。",
                    backToMyJobs: "← 我的发布",
                    backToApplicants: "← 申请人列表",
                    failedPublishJob: "发布职位失败，请检查输入后重试。",
                    jobPostedSuccess: "职位发布成功。",
                    networkErrorPostingJob: "发布职位时网络异常。",
                    unableLoadPostings: "当前无法加载发布记录。",
                    noJobsPostedYet: "暂未发布任何职位。",
                    youHavePosted: "你已发布",
                    noPostingsYetTitle: "暂无发布记录",
                    publishFirstTaPosition: "请使用左侧表单发布你的第一个 TA 职位。",
                    reviewApplicants: "审核申请人",
                    untitledPosition: "未命名职位",
                    overviewPartialLoad: "部分概览数据加载失败，已展示可用结果。",
                    unableLoadOverview: "当前无法加载概览数据。",
                    moOnlyPage: "该页面仅 MO 账号可访问。",
                    noActivityYet: "暂无活动记录。",
                    tracking: "跟踪",
                    noRecentActivityTitle: "暂无最近活动",
                    latestUpdatesAppear: "当 TA 申请你发布的职位后，最新动态会显示在这里。",
                    newApplicationReceived: "收到新申请",
                    offerAccepted: "录用已接受",
                    applicationAccepted: "申请已通过。",
                    applicationRejected: "申请已拒绝。",
                    applicationWithdrawn: "申请已撤回",
                    applicationUpdated: "申请已更新",
                    unknownApplicant: "未知申请人",
                    failedLoadApplicationTotals: "加载申请总量失败。",
                    failedLoadMoWorkloads: "加载 MO 工作量失败。",
                    failedLoadTaWorkloads: "加载 TA 工作量失败。",
                    networkErrorLoadingDashboard: "加载仪表盘时网络异常。",
                    exporting: "导出中...",
                    csvExportedSuccess: "CSV 导出成功。",
                    unableExportCsv: "无法导出 CSV。",
                    noMoWorkloadSelectedRange: "所选时间范围内暂无 MO 工作量数据。",
                    noTaWorkloadSelectedRange: "所选工作期内暂无 TA 工作量数据。",
                    loaded: "已加载",
                    moWorkloadItemUnit: "条 MO 工作量",
                    taWorkloadItemUnit: "条 TA 工作量",
                    noStatusData: "暂无状态数据。",
                    noMoWorkloadData: "暂无 MO 工作量数据。",
                    noTaWorkloadData: "暂无 TA 工作量数据。",
                    noWorkloadDataYetTitle: "暂无工作量数据",
                    adjustTimeRangeHint: "请调整时间范围，或等待申请活动产生后再查看。",
                    adjustWorkRangeHint: "请调整工作期范围，或等待出现已通过职位后再查看。",
                    sessionExpiredRedirect: "会话已过期，正在跳转到登录页...",
                    invalidDatetimeFormat: "日期时间格式无效。",
                    startAfterEnd: "开始时间不能晚于结束时间。",
                    networkErrorTryAgain: "网络异常，请重试。",
                    networkErrorMoment: "网络异常，请稍后重试。",
                    currentCompleteness: "当前完整度："
                }
            },
            server: {
                auth: {
                    loginRequired: "请先登录。",
                    unauthorized: "请先登录。",
                    forbidden: "你没有权限访问该资源。",
                    accessDenied: "访问被拒绝。",
                    noResourcePermission: "你没有权限访问该资源。",
                    noActionPermission: "你没有权限执行此操作。",
                    invalidRoleParameter: "角色参数无效。",
                    roleMismatch: "所选角色与该账号不匹配。",
                    invalidCredentials: "用户名或邮箱或密码输入有误。",
                    loginError: "登录过程中出现错误，请稍后重试。",
                    loginSuccess: "登录成功。",
                    logoutSuccess: "退出登录成功。",
                    registrationSuccess: "注册成功。",
                    usernameRequired: "用户名不能为空。",
                    usernameTooLong: "用户名过长。",
                    usernameInvalid: "用户名格式无效。",
                    usernameConsecutiveUnderscore: "用户名不能包含连续下划线。",
                    usernameTrailingUnderscore: "用户名不能以下划线结尾。",
                    usernameExists: "该用户名已被注册。",
                    emailRequired: "邮箱地址不能为空。",
                    emailTooLong: "邮箱地址过长。",
                    emailInvalid: "请输入有效的邮箱地址。",
                    emailExists: "该邮箱已被注册。",
                    passwordRequired: "密码不能为空。",
                    passwordTooShort6: "密码长度至少为 6 位。",
                    passwordTooShort8: "密码长度至少为 8 位。",
                    passwordTooLong: "密码过长。",
                    passwordTooSimple: "密码必须包含至少一个字母和一个数字。",
                    passwordMismatch: "两次输入的密码不一致。"
                },
                adminInvite: {
                    codeRequired: "邀请码不能为空。",
                    codeInvalidOrExpired: "邀请码无效或已过期。",
                    accountCreated: "管理员账号创建成功。",
                    accountCreateFailed: "创建管理员账号失败。",
                    adminAccessRequired: "需要管理员权限。",
                    codeRotated: "邀请码已刷新。"
                },
                account: {
                    profileRetrieved: "账号资料已加载。",
                    profileUpdated: "账号资料已更新。",
                    onlyTaMoUpdate: "只有 TA 或 MO 账号可以更新账号资料。",
                    displayNameTooLong: "昵称过长。",
                    realNameTooLong: "真实姓名过长。",
                    professionalTitleTooLong: "头衔过长。",
                    unsupportedChars: "账号资料包含不支持的字符。",
                    avatarTooLarge: "头像文件过大。",
                    avatarInvalidType: "头像必须是 JPG、PNG 或 WEBP。"
                },
                applicant: {
                    profileNotFound: "未找到申请人档案。",
                    profileRetrieved: "申请人档案已加载。",
                    profileCreated: "申请人档案已创建。",
                    profileUpdated: "申请人档案已更新。",
                    profileExistsForUser: "该账号已存在申请人档案。",
                    studentIdExists: "该学号已被注册。",
                    resumeRequiredBeforeSave: "请先上传简历，再保存档案。",
                    resumeDraftUploaded: "简历草稿已上传。",
                    resumeDraftDiscarded: "简历草稿已丢弃。",
                    detailRetrieved: "申请人详情已加载。",
                    noAccess: "你没有权限查看该申请人。",
                    photoNotFound: "未找到个人照片。",
                    resumeFileUnavailable: "简历文件暂不可用。",
                    taProfileRequired: "未找到 TA 档案，请先完善个人档案。",
                    notFoundWithId: "未找到申请人。",
                    fileUploadFailed: "文件上传失败。",
                    fileInvalidType: "文件类型无效。",
                    fileInvalidExtension: "文件扩展名无效。",
                    fileTooLarge: "文件大小超过限制。",
                    experienceTooLong: "相关经历过长。",
                    motivationTooLong: "申请动机过长。",
                    addressTooLong: "地址过长。",
                    addressPunctuation: "地址不能只包含标点符号。"
                },
                job: {
                    listRetrieved: "职位列表已加载。",
                    retrieved: "职位已加载。",
                    notFound: "未找到职位。",
                    onlyMoPost: "仅 MO 账号可发布职位。",
                    created: "职位已创建。",
                    updated: "职位已更新。",
                    deleted: "职位已删除。",
                    updateOwnOnly: "只能更新自己发布的职位。",
                    deleteOwnOnly: "只能删除自己发布的职位。",
                    deleteFailed: "删除职位失败。",
                    statusRequired: "请选择职位状态。",
                    idRequiredLower: "缺少 jobId。",
                    idInvalidChars: "jobId 包含不支持的字符。"
                },
                application: {
                    idRequiredLower: "缺少 applicationId。",
                    notFound: "未找到申请。",
                    listRetrieved: "申请列表已加载。",
                    retrieved: "申请已加载。",
                    submitted: "申请已提交。",
                    accepted: "申请已通过。",
                    rejected: "申请已拒绝。",
                    withdrawn: "申请已撤回。",
                    actionRequired: "缺少操作类型。",
                    jobNotFound: "未找到该申请对应的职位。",
                    onlyTaApply: "仅 TA 账号可提交申请。",
                    alreadyApplied: "你已经申请过该职位。",
                    alreadyReviewed: "该申请已审核完毕。",
                    withdrawFailed: "撤回申请失败。",
                    withdrawUnavailable: "该申请无法撤回。",
                    reviewOwnJobsOnly: "只能审核自己发布职位的申请。"
                },
                workload: {
                    adminOnly: "只有管理员可以查看工作量统计。",
                    invalidStart: "工作期开始时间无效。",
                    invalidEnd: "工作期结束时间无效。",
                    startAfterEnd: "开始日期不能晚于结束日期。",
                    onlyTaSupported: "当前只支持 TA 工作量统计。",
                    generated: "工作量报表已生成。",
                    jobRecordMissing: "缺少职位记录。",
                    weeklyHoursMissing: "缺少每周小时数。",
                    weeklyHoursDecimal: "每周小时数最多支持 1 位小数。",
                    startDateMissing: "缺少工作开始日期。",
                    endDateMissing: "缺少工作结束日期。",
                    endBeforeStart: "工作结束日期不能早于工作开始日期。"
                },
                search: {
                    queryTooLong: "搜索内容过长。",
                    queryInvalidChars: "搜索内容包含不支持的字符。",
                    ownJobsOnly: "只能搜索自己发布职位的申请人。"
                },
                ai: {
                    moApplicantOnly: "只有 MO 账号可以使用申请人 AI 搜索。",
                    taJobOnly: "只有 TA 账号可以使用职位 AI 搜索。",
                    applicantSearchUnavailable: "AI 搜索暂不可用，请稍后再试。",
                    jobSearchUnavailable: "AI 推荐暂不可用，请稍后再试。",
                    profileRequired: "请先完善个人档案后再使用 AI 推荐。",
                    applicantRecommendationsGenerated: "已生成 AI 推荐结果。",
                    jobRecommendationsGenerated: "已生成 AI 推荐职位。"
                },
                notifications: {
                    titleRequired: "标题不能为空。",
                    contentRequired: "正文不能为空。",
                    notFound: "未找到通知。"
                },
                common: {
                    endpointNotFound: "接口不存在。",
                    deleteUnsupported: "不支持删除操作。",
                    methodNotAllowed: "请求方法不被允许。"
                }
            }
        }
    };

    var currentLocale = DEFAULT_LOCALE;

    /*
     * 统一外部传入的语言值。
     * 只支持 en 和 zh-CN，其他区域码统一折叠到这两个页面语言。
     */
    function normalizeLocale(input) {
        if (typeof input !== "string" || !input.trim()) {
            return "";
        }
        var normalized = input.trim().toLowerCase();
        if (normalized === "en" || normalized.indexOf("en-") === 0) {
            return "en";
        }
        if (normalized === "zh" || normalized === "zh-cn" || normalized.indexOf("zh-") === 0) {
            return CHINESE_LOCALE;
        }
        return "";
    }

    /*
     * 读取用户上次选择的语言。
     */
    function readSavedLocale() {
        try {
            return normalizeLocale(window.localStorage.getItem(STORAGE_KEY) || "");
        } catch (error) {
            return "";
        }
    }

    /*
     * 从浏览器语言中推断首选语言。
     */
    function readBrowserLocale() {
        var languages = [];
        if (Array.isArray(window.navigator.languages)) {
            languages = window.navigator.languages.slice();
        }
        if (typeof window.navigator.language === "string" && window.navigator.language) {
            languages.push(window.navigator.language);
        }
        for (var i = 0; i < languages.length; i += 1) {
            var candidate = normalizeLocale(languages[i]);
            if (candidate) {
                return candidate;
            }
        }
        return "";
    }

    /*
     * 初始语言优先级：用户保存值 -> locale-bootstrap 标记 -> 浏览器语言 -> 英文。
     */
    function resolveInitialLocale() {
        return readSavedLocale() || normalizeLocale(document.documentElement.getAttribute("data-initial-locale") || "") || readBrowserLocale() || DEFAULT_LOCALE;
    }

    /*
     * 通过点号路径读取字典值，例如 portal.taJobList.searchPlaceholder。
     */
    function getByPath(locale, key) {
        if (!locale || !key) {
            return "";
        }
        var target = dictionaries[locale];
        if (!target) {
            return "";
        }
        var parts = key.split(".");
        var value = target;
        for (var i = 0; i < parts.length; i += 1) {
            if (!value || typeof value !== "object" || !Object.prototype.hasOwnProperty.call(value, parts[i])) {
                return "";
            }
            value = value[parts[i]];
        }
        return typeof value === "string" ? value : "";
    }

    /*
     * 页面和动态脚本统一使用的翻译函数。
     */
    function t(key, fallback) {
        var localized = getByPath(currentLocale, key) || getByPath(DEFAULT_LOCALE, key);
        if (localized) {
            return localized;
        }
        return typeof fallback === "string" ? fallback : key;
    }

    /*
     * 服务端英文消息到 i18n key 的映射。
     * 遗留/待移除：如果后端未来直接返回稳定错误 code，这张英文文本映射表可以删除。
     */
    var SERVER_MESSAGE_KEYS = {
        "please login first": "server.auth.loginRequired",
        "please log in first": "server.auth.loginRequired",
        "unauthorized": "server.auth.unauthorized",
        "forbidden": "server.auth.forbidden",
        "access denied": "server.auth.accessDenied",
        "you do not have permission to access this resource": "server.auth.noResourcePermission",
        "you don't have permission to access this resource": "server.auth.noResourcePermission",
        "you do not have permission to perform this action": "server.auth.noActionPermission",
        "invalid role parameter": "server.auth.invalidRoleParameter",
        "role mismatch": "server.auth.roleMismatch",
        "invalid username/email or password": "server.auth.invalidCredentials",
        "invalid username or password": "server.auth.invalidCredentials",
        "selected login role does not match account role": "server.auth.roleMismatch",
        "an error occurred during login. please try again later": "server.auth.loginError",
        "username or email is required": "login.msg.enterIdentifier",
        "login successful": "server.auth.loginSuccess",
        "logout successful": "server.auth.logoutSuccess",
        "registration successful": "server.auth.registrationSuccess",
        "username is required": "server.auth.usernameRequired",
        "username is too long": "server.auth.usernameTooLong",
        "username contains unsupported characters": "register.msg.usernameUnsupported",
        "username format is invalid": "server.auth.usernameInvalid",
        "username must start with a letter and contain 3-20 letters, numbers, or underscores": "register.msg.usernameInvalid",
        "must start with a letter, 3-20 letters/numbers/underscores": "register.msg.usernameInvalid",
        "username cannot contain consecutive underscores": "server.auth.usernameConsecutiveUnderscore",
        "username cannot end with an underscore": "server.auth.usernameTrailingUnderscore",
        "username already exists": "server.auth.usernameExists",
        "email is required": "server.auth.emailRequired",
        "email address is required": "server.auth.emailRequired",
        "email is too long": "server.auth.emailTooLong",
        "email address is too long": "server.auth.emailTooLong",
        "invalid email format": "server.auth.emailInvalid",
        "email already exists": "server.auth.emailExists",
        "password is required": "server.auth.passwordRequired",
        "password must be at least 6 characters": "server.auth.passwordTooShort6",
        "password must be at least 8 characters": "server.auth.passwordTooShort8",
        "password is too long": "server.auth.passwordTooLong",
        "password must contain at least one letter and one number": "server.auth.passwordTooSimple",
        "passwords do not match": "server.auth.passwordMismatch",
        "invite code is required": "server.adminInvite.codeRequired",
        "invite code is invalid or expired": "server.adminInvite.codeInvalidOrExpired",
        "admin account created": "server.adminInvite.accountCreated",
        "failed to create admin account": "server.adminInvite.accountCreateFailed",
        "only admin users can create invite codes": "server.adminInvite.adminOnlyCreate",
        "admin access required": "server.adminInvite.adminAccessRequired",
        "admin access is required": "server.adminInvite.adminAccessRequired",
        "invite code refreshed": "server.adminInvite.codeRotated",
        "account profile retrieved successfully": "server.account.profileRetrieved",
        "account profile updated successfully": "server.account.profileUpdated",
        "only ta accounts can update account profile": "server.account.onlyTaMoUpdate",
        "only ta or mo accounts can update account profile": "server.account.onlyTaMoUpdate",
        "display name is too long": "server.account.displayNameTooLong",
        "real name is too long": "server.account.realNameTooLong",
        "professional title is too long": "server.account.professionalTitleTooLong",
        "account profile contains unsupported characters": "server.account.unsupportedChars",
        "avatar file is too large": "server.account.avatarTooLarge",
        "avatar must be jpg, png, or webp": "server.account.avatarInvalidType",
        "endpoint not found": "server.common.endpointNotFound",
        "delete is not supported": "server.common.deleteUnsupported",
        "method not allowed": "server.common.methodNotAllowed",
        "applicant profile not found": "server.applicant.profileNotFound",
        "applicant profile retrieved": "server.applicant.profileRetrieved",
        "applicant profile created": "server.applicant.profileCreated",
        "applicant profile updated": "server.applicant.profileUpdated",
        "student id already exists": "server.applicant.studentIdExists",
        "please upload your resume before saving your profile": "server.applicant.resumeRequiredBeforeSave",
        "resume draft uploaded": "server.applicant.resumeDraftUploaded",
        "resume draft discarded": "server.applicant.resumeDraftDiscarded",
        "applicant detail retrieved": "server.applicant.detailRetrieved",
        "you do not have access to this applicant": "server.applicant.noAccess",
        "profile photo not found": "server.applicant.photoNotFound",
        "resume file is unavailable": "server.applicant.resumeFileUnavailable",
        "ta profile not found. please complete your profile first": "server.applicant.taProfileRequired",
        "applicant profile not found. please create one first": "server.applicant.profileNotFound",
        "full name is required": "portal.taDashboard.validation.fullName.required",
        "student id is required": "portal.taDashboard.validation.studentId.required",
        "department is required": "portal.taDashboard.validation.department.required",
        "program is required": "portal.taDashboard.validation.program.required",
        "gpa is required": "portal.taDashboard.validation.gpa.required",
        "skills are required": "portal.taDashboard.validation.skills.required",
        "phone number is required": "portal.taDashboard.validation.phone.required",
        "related experience is required": "portal.taDashboard.validation.experience.required",
        "motivation is required": "portal.taDashboard.validation.motivation.required",
        "full name must be at least 2 characters": "portal.taDashboard.validation.fullName.tooShort",
        "full name must be 100 characters or fewer": "portal.taDashboard.validation.fullName.tooLong",
        "full name must include at least one letter": "portal.taDashboard.validation.fullName.noLetter",
        "student id must be exactly 10 digits, for example 2023213039": "portal.taDashboard.validation.studentId.notTenDigits",
        "student id year appears invalid. please check the first 4 digits": "portal.taDashboard.validation.studentId.invalidYear",
        "student id appears invalid. please check your official 10-digit student number": "portal.taDashboard.validation.studentId.allSameDigit",
        "department must be at least 2 characters": "portal.taDashboard.validation.department.tooShort",
        "department must be 100 characters or fewer": "portal.taDashboard.validation.department.tooLong",
        "gpa must be 20 characters or fewer": "portal.taDashboard.validation.gpa.tooLong",
        "gpa may only include digits, spaces, decimal separators, and '/'": "portal.taDashboard.validation.gpa.invalidChars",
        "gpa format is invalid. use one optional '/'": "portal.taDashboard.validation.gpa.multipleSlash",
        "skills must be 300 characters or fewer": "portal.taDashboard.validation.skills.tooLong",
        "duplicate skills found. please keep each skill only once": "portal.taDashboard.validation.skills.duplicate",
        "phone number must be 30 characters or fewer": "portal.taDashboard.validation.phone.tooLong",
        "phone number may only include digits, spaces, and + - ( ) . /": "portal.taDashboard.validation.phone.invalidChars",
        "phone number can contain only one '+'": "portal.taDashboard.validation.phone.multiplePlus",
        "if used, '+' must be at the beginning": "portal.taDashboard.validation.phone.plusNotAtStart",
        "phone number appears invalid. please check repeated digits": "portal.taDashboard.validation.phone.allSameDigit",
        "address must be 200 characters or fewer": "server.applicant.addressTooLong",
        "address cannot contain only punctuation": "server.applicant.addressPunctuation",
        "job list retrieved": "server.job.listRetrieved",
        "jobs retrieved successfully": "server.job.listRetrieved",
        "job retrieved": "server.job.retrieved",
        "job retrieved successfully": "server.job.retrieved",
        "job not found": "server.job.notFound",
        "only mo accounts can publish jobs": "server.job.onlyMoPost",
        "job created": "server.job.created",
        "job created successfully": "server.job.created",
        "job updated": "server.job.updated",
        "job updated successfully": "server.job.updated",
        "job deleted": "server.job.deleted",
        "job deleted successfully": "server.job.deleted",
        "you can only update your own jobs": "server.job.updateOwnOnly",
        "you can only delete your own jobs": "server.job.deleteOwnOnly",
        "failed to delete job": "server.job.deleteFailed",
        "job title is required": "portal.moDashboard.validationJobTitleRequired",
        "course code is required": "portal.moDashboard.validationCourseCodeRequired",
        "course name is required": "portal.moDashboard.validationCourseNameRequired",
        "description is required": "portal.moDashboard.validationDescriptionRequired",
        "required skills are required": "portal.moDashboard.validationSkillsRequired",
        "positions must be a whole number": "portal.moDashboard.validationPositionsNumber",
        "positions must be between 1 and 200": "portal.moDashboard.validationPositionsRange",
        "weekly hours are required": "portal.moDashboard.validationWeeklyHoursRequired",
        "weekly hours must be a number with at most one decimal place": "portal.moDashboard.validationWeeklyHoursNumber",
        "weekly hours must be between 0.5 and 40": "portal.moDashboard.validationWeeklyHoursRange",
        "work start date is required": "server.workload.startDateMissing",
        "work end date is required": "server.workload.endDateMissing",
        "please use english commas or chinese commas to separate skills": "portal.moDashboard.validationSkillsCommaSeparator",
        "duplicate skills found. please keep each skill only once": "portal.moDashboard.validationSkillsDuplicate",
        "work start date cannot be before application deadline": "portal.moDashboard.validationWorkStartBeforeDeadline",
        "work end date cannot be before work start date": "portal.moDashboard.validationWorkPeriodOrder",
        "salary is required": "portal.moDashboard.validationSalaryRequired",
        "application deadline is required": "portal.moDashboard.validationDeadlineRequired",
        "status is required": "server.job.statusRequired",
        "jobid is required": "server.job.idRequiredLower",
        "job id is required": "server.job.idRequiredLower",
        "jobid contains unsupported characters": "server.job.idInvalidChars",
        "jobid contains invalid characters": "server.job.idInvalidChars",
        "job id contains unsupported characters": "server.job.idInvalidChars",
        "job id contains invalid characters": "server.job.idInvalidChars",
        "applicationid is required": "server.application.idRequiredLower",
        "application id is required": "server.application.idRequiredLower",
        "application not found": "server.application.notFound",
        "application list retrieved": "server.application.listRetrieved",
        "applications retrieved successfully": "server.application.listRetrieved",
        "application retrieved": "server.application.retrieved",
        "application retrieved successfully": "server.application.retrieved",
        "application submitted": "server.application.submitted",
        "application submitted successfully": "server.application.submitted",
        "application accepted": "server.application.accepted",
        "application accepted successfully": "server.application.accepted",
        "application rejected": "server.application.rejected",
        "application rejected successfully": "server.application.rejected",
        "application withdrawn": "server.application.withdrawn",
        "application withdrawn successfully": "server.application.withdrawn",
        "failed to withdraw application": "server.application.withdrawFailed",
        "action is required": "server.application.actionRequired",
        "job not found for this application": "server.application.jobNotFound",
        "only ta accounts can submit applications": "server.application.onlyTaApply",
        "you have already applied for this job": "server.application.alreadyApplied",
        "cover letter must be 2000 characters or fewer": "portal.dynamic.coverLetterTooLong",
        "this application has already been reviewed": "server.application.alreadyReviewed",
        "this application cannot be withdrawn": "server.application.withdrawUnavailable",
        "this application can no longer be withdrawn": "server.application.withdrawUnavailable",
        "you don't have permission to view this application": "server.auth.noResourcePermission",
        "you don't have permission to withdraw this application": "server.auth.noActionPermission",
        "you can only review applications for your own jobs": "server.application.reviewOwnJobsOnly",
        "only admin accounts can view workload statistics": "server.workload.adminOnly",
        "invalid work range start": "server.workload.invalidStart",
        "invalid work range end": "server.workload.invalidEnd",
        "start date cannot be after end date": "server.workload.startAfterEnd",
        "only ta workload statistics are supported": "server.workload.onlyTaSupported",
        "workload report generated": "server.workload.generated",
        "job record is missing": "server.workload.jobRecordMissing",
        "job record not found": "server.workload.jobRecordMissing",
        "weekly hours are missing": "server.workload.weeklyHoursMissing",
        "weekly hours must use at most one decimal place": "server.workload.weeklyHoursDecimal",
        "weekly hours must have at most one decimal place": "server.workload.weeklyHoursDecimal",
        "work start date is missing": "server.workload.startDateMissing",
        "work end date is missing": "server.workload.endDateMissing",
        "work end date cannot be before work start date": "server.workload.endBeforeStart",
        "search query is too long": "server.search.queryTooLong",
        "search query contains unsupported characters": "server.search.queryInvalidChars",
        "query is too long": "server.search.queryTooLong",
        "query contains invalid characters": "server.search.queryInvalidChars",
        "you can only search applicants for your own jobs": "server.search.ownJobsOnly",
        "only mo can use applicant ai search": "server.ai.moApplicantOnly",
        "only ta can use job ai search": "server.ai.taJobOnly",
        "ai 搜索暂不可用，请稍后再试": "server.ai.applicantSearchUnavailable",
        "ai 推荐暂不可用，请稍后再试": "server.ai.jobSearchUnavailable",
        "请先完善个人档案后再使用 ai 推荐": "server.ai.profileRequired",
        "已生成 ai 推荐结果": "server.ai.applicantRecommendationsGenerated",
        "已生成 ai 推荐职位": "server.ai.jobRecommendationsGenerated",
        "title is required": "server.notifications.titleRequired",
        "content is required": "server.notifications.contentRequired",
        "notification not found": "server.notifications.notFound"
    };

    /*
     * 服务端带变量前缀的消息映射，例如 “username already exists: xxx”。
     */
    var SERVER_MESSAGE_PREFIX_KEYS = [
        { prefix: "username already exists:", key: "server.auth.usernameExists" },
        { prefix: "email already exists:", key: "server.auth.emailExists" },
        { prefix: "applicant profile already exists for user:", key: "server.applicant.profileExistsForUser" },
        { prefix: "student id already exists:", key: "server.applicant.studentIdExists" },
        { prefix: "applicant not found:", key: "server.applicant.notFoundWithId" },
        { prefix: "file upload failed", key: "server.applicant.fileUploadFailed" },
        { prefix: "invalid file type. only", key: "server.applicant.fileInvalidType" },
        { prefix: "invalid file extension. only", key: "server.applicant.fileInvalidExtension" },
        { prefix: "file size exceeds", key: "server.applicant.fileTooLarge" },
        { prefix: "related experience must be", key: "server.applicant.experienceTooLong" },
        { prefix: "motivation must be", key: "server.applicant.motivationTooLong" }
    ];

    /*
     * 归一化服务端消息，去掉句号和多余空格后再匹配。
     */
    function normalizeServerMessage(message) {
        return String(message || "")
            .replace(/\s+/g, " ")
            .trim()
            .replace(/[.!。]+$/g, "")
            .toLowerCase();
    }

    /*
     * 把服务端消息解析成翻译 key。
     */
    function resolveServerMessageKey(message) {
        var normalized = normalizeServerMessage(message);
        if (!normalized) {
            return "";
        }
        if (Object.prototype.hasOwnProperty.call(SERVER_MESSAGE_KEYS, normalized)) {
            return SERVER_MESSAGE_KEYS[normalized];
        }
        for (var i = 0; i < SERVER_MESSAGE_PREFIX_KEYS.length; i += 1) {
            if (normalized.indexOf(SERVER_MESSAGE_PREFIX_KEYS[i].prefix) === 0) {
                return SERVER_MESSAGE_PREFIX_KEYS[i].key;
            }
        }
        return "";
    }

    /*
     * 动态错误本地化入口。
     * 优先翻译已知服务端消息，未知消息保留原文，避免丢失调试信息。
     */
    function localizeServerMessage(message, fallbackKey, fallbackText) {
        var raw = typeof message === "string" ? message.trim() : "";
        if (raw) {
            var key = resolveServerMessageKey(raw);
            if (key) {
                return t(key, raw);
            }
            return raw;
        }
        if (fallbackKey) {
            return t(fallbackKey, fallbackText);
        }
        return typeof fallbackText === "string" ? fallbackText : "";
    }

    /*
     * 记住用户手动切换的语言。
     */
    function rememberLocale(locale) {
        try {
            window.localStorage.setItem(STORAGE_KEY, locale);
        } catch (error) {
            // Ignore storage failures (private mode, browser policy, etc.).
        }
    }

    /*
     * 刷新 data-i18n 标记的文本节点。
     */
    function updateTextContent() {
        var textNodes = document.querySelectorAll("[data-i18n]");
        Array.prototype.forEach.call(textNodes, function (node) {
            var key = node.getAttribute("data-i18n");
            if (!node.hasAttribute("data-i18n-default")) {
                node.setAttribute("data-i18n-default", node.textContent);
            }
            node.textContent = t(key, node.getAttribute("data-i18n-default") || "");
        });
    }

    /*
     * 刷新 placeholder/title/aria-label/alt/value 等属性翻译。
     */
    function updateAttribute(selector, keyAttribute, targetAttribute, defaultStoreAttribute) {
        var nodes = document.querySelectorAll(selector);
        Array.prototype.forEach.call(nodes, function (node) {
            var key = node.getAttribute(keyAttribute);
            if (!node.hasAttribute(defaultStoreAttribute)) {
                node.setAttribute(defaultStoreAttribute, node.getAttribute(targetAttribute) || "");
            }
            node.setAttribute(targetAttribute, t(key, node.getAttribute(defaultStoreAttribute) || ""));
        });
    }

    /*
     * 同步语言切换按钮的激活态和 aria-pressed。
     */
    function syncLocaleButtons() {
        var switchers = document.querySelectorAll("[data-locale-switch]");
        Array.prototype.forEach.call(switchers, function (button) {
            var buttonLocale = normalizeLocale(button.getAttribute("data-locale") || "");
            var active = buttonLocale === currentLocale;
            button.classList.toggle("is-active", active);
            button.setAttribute("aria-pressed", active ? "true" : "false");
        });
    }

    /*
     * 应用语言到页面。
     * 末尾派发 app:locale-changed，通知动态渲染的 JS 卡片重新绘制。
     */
    function applyLocale(locale, persist) {
        var normalized = normalizeLocale(locale) || DEFAULT_LOCALE;
        currentLocale = normalized;
        document.documentElement.setAttribute("lang", normalized === CHINESE_LOCALE ? CHINESE_LOCALE : "en");

        updateTextContent();
        updateAttribute("[data-i18n-placeholder]", "data-i18n-placeholder", "placeholder", "data-i18n-placeholder-default");
        updateAttribute("[data-i18n-aria-label]", "data-i18n-aria-label", "aria-label", "data-i18n-aria-label-default");
        updateAttribute("[data-i18n-title]", "data-i18n-title", "title", "data-i18n-title-default");
        updateAttribute("[data-i18n-alt]", "data-i18n-alt", "alt", "data-i18n-alt-default");
        updateAttribute("[data-i18n-value]", "data-i18n-value", "value", "data-i18n-value-default");
        syncLocaleButtons();
        document.documentElement.classList.remove("i18n-pending");

        if (persist) {
            rememberLocale(normalized);
        }

        document.dispatchEvent(new CustomEvent("app:locale-changed", { detail: { locale: normalized } }));
    }

    /*
     * 绑定所有语言切换按钮。
     */
    function bindLocaleButtons() {
        var switchers = document.querySelectorAll("[data-locale-switch]");
        Array.prototype.forEach.call(switchers, function (button) {
            button.addEventListener("click", function () {
                var targetLocale = normalizeLocale(button.getAttribute("data-locale") || "");
                if (!targetLocale || targetLocale === currentLocale) {
                    return;
                }
                applyLocale(targetLocale, true);
            });
        });
    }

    /*
     * 对外暴露给页面脚本的最小 i18n API。
     */
    window.AppI18n = {
        t: t,
        localizeServerMessage: localizeServerMessage,
        getLocale: function () {
            return currentLocale;
        },
        setLocale: function (locale) {
            applyLocale(locale, true);
        },
        apply: function () {
            applyLocale(currentLocale, false);
        }
    };

    /*
     * 页面初始化：绑定按钮并应用初始语言。
     */
    function initialize() {
        bindLocaleButtons();
        applyLocale(resolveInitialLocale(), false);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initialize);
    } else {
        initialize();
    }
})();
