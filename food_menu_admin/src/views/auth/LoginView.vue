<template>
  <div class="login-stage" @mousemove="handleMouseMove">
    <div class="theme-toggle-wrapper">
      <ThemeToggle />
    </div>
    
    <!-- Animated background elements -->
    <div class="orbit-shape shape-1"></div>
    <div class="orbit-shape shape-2"></div>
    
    <div class="login-card glass-card hover-rise fade-in-up" :class="{ 'shake-animation': loginState === 'fail' }">
      <!-- Left Panel: Visual & Characters -->
      <div class="login-visual" :class="visualStateClasses">
        <svg width="100%" height="100%" viewBox="0 0 400 300" preserveAspectRatio="xMidYMid meet" fill="none" xmlns="http://www.w3.org/2000/svg" id="login-characters" ref="svgRef">
          <!-- 
            Composition: Tight Cluster
            Order: Black (Back), Purple (Back), Yellow (Front), Orange (Front)
            Structure: Body Group (Layer 3) -> Body Shape (Breathing) -> Face Group (Layer 2 - Clamped) -> Hands (Layer 3 - Top) -> Pupil Group (Layer 1)
          -->
          
          <!-- Black: Tall Blob (Back Middle) - Normal Friction -->
          <g id="char-black" class="character" :style="bodyStyle">
            <path class="body-shape" d="M170 50 C 170 20 230 20 230 50 V 280 C 230 295 170 295 170 280 Z" fill="#1a1a1a" :style="getBreathStyle(0)"/>
            <!-- Face Group (Clamped) -->
            <g class="face-group" :style="getFaceStyle(200, 90, 'normal')">
              <!-- Eyes -->
              <g transform="translate(190, 90)">
                <g class="eye-container" :class="{ 'blinking': blinkingState.black }">
                  <circle class="sclera" cx="0" cy="0" r="8" fill="white"/>
                  <g class="pupil-group" :style="getPupilStyle(190, 90, 'normal')">
                    <circle class="pupil" cx="0" cy="0" r="3" fill="black"/>
                  </g>
                  <!-- Eyelid (Blink & Shy) -->
                  <rect class="eyelid" x="-10" y="-10" width="20" height="0" fill="#1a1a1a" />
                </g>
              </g>
              <g transform="translate(210, 90)">
                <g class="eye-container" :class="{ 'blinking': blinkingState.black }">
                  <circle class="sclera" cx="0" cy="0" r="8" fill="white"/>
                  <g class="pupil-group" :style="getPupilStyle(210, 90, 'normal')">
                    <circle class="pupil" cx="0" cy="0" r="3" fill="black"/>
                  </g>
                  <!-- Eyelid -->
                  <rect class="eyelid" x="-10" y="-10" width="20" height="0" fill="#1a1a1a" />
                </g>
              </g>
              <!-- Mouth -->
              <path class="mouth" d="M195 110 Q200 115 205 110" stroke="white" stroke-width="2" stroke-linecap="round" fill="none"/>
            </g>
          </g>

          <!-- Purple: Boxy Blob (Back Left) - Normal Friction -->
          <g id="char-purple" class="character" :style="bodyStyle">
            <path class="body-shape" d="M80 80 C 80 60 160 60 160 80 V 280 C 160 295 80 295 80 280 Z" fill="#6c5ce7" :style="getBreathStyle(1)"/>
            <!-- Face Group -->
            <g class="face-group" :style="getFaceStyle(120, 120, 'normal')">
              <!-- Eyes -->
              <g transform="translate(105, 120)">
                <g class="eye-container" :class="{ 'blinking': blinkingState.purple }">
                  <circle class="sclera" cx="0" cy="0" r="10" fill="white"/>
                  <g class="pupil-group" :style="getPupilStyle(105, 120, 'normal')">
                    <circle class="pupil" cx="0" cy="0" r="4" fill="black"/>
                  </g>
                  <!-- Eyelid -->
                  <rect class="eyelid" x="-12" y="-12" width="24" height="0" fill="#6c5ce7" />
                </g>
              </g>
              <g transform="translate(135, 120)">
                <g class="eye-container" :class="{ 'blinking': blinkingState.purple }">
                  <circle class="sclera" cx="0" cy="0" r="10" fill="white"/>
                  <g class="pupil-group" :style="getPupilStyle(135, 120, 'normal')">
                    <circle class="pupil" cx="0" cy="0" r="4" fill="black"/>
                  </g>
                  <!-- Eyelid -->
                  <rect class="eyelid" x="-12" y="-12" width="24" height="0" fill="#6c5ce7" />
                </g>
              </g>
              <!-- Mouth -->
              <path class="mouth" d="M110 150 Q120 160 130 150" stroke="#333" stroke-width="2" stroke-linecap="round" fill="none"/>
            </g>
          </g>

          <!-- Yellow: Small Blob (Front Right) - Agile Friction -->
          <g id="char-yellow" class="character" :style="bodyStyle">
            <path class="body-shape" d="M190 300 C 190 240 270 240 270 300 Z" fill="#ffeaa7" :style="getBreathStyle(2)"/>
            <!-- Face Group -->
            <g class="face-group" :style="getFaceStyle(230, 275, 'agile')">
              <!-- Eyes -->
              <g transform="translate(220, 275)">
                <g class="eye-container" :class="{ 'blinking': blinkingState.yellow }">
                  <circle class="sclera" cx="0" cy="0" r="6" fill="white"/>
                  <g class="pupil-group" :style="getPupilStyle(220, 275, 'agile')">
                    <circle class="pupil" cx="0" cy="0" r="2" fill="black"/>
                  </g>
                  <!-- Eyelid -->
                  <rect class="eyelid" x="-8" y="-8" width="16" height="0" fill="#ffeaa7" />
                </g>
              </g>
              <g transform="translate(240, 275)">
                <g class="eye-container" :class="{ 'blinking': blinkingState.yellow }">
                  <circle class="sclera" cx="0" cy="0" r="6" fill="white"/>
                  <g class="pupil-group" :style="getPupilStyle(240, 275, 'agile')">
                    <circle class="pupil" cx="0" cy="0" r="2" fill="black"/>
                  </g>
                  <!-- Eyelid -->
                  <rect class="eyelid" x="-8" y="-8" width="16" height="0" fill="#ffeaa7" />
                </g>
              </g>
              <!-- Mouth -->
              <path class="mouth" d="M225 285 Q230 288 235 285" stroke="#333" stroke-width="1.5" stroke-linecap="round" fill="none"/>
            </g>
          </g>

          <!-- Orange: Round Blob (Front Left - Anchor) - Sluggish Friction -->
          <g id="char-orange" class="character" :style="bodyStyle">
            <path class="body-shape" d="M70 300 C 70 200 200 200 200 300 Z" fill="#e17055" :style="getBreathStyle(3)"/>
            <!-- Face Group -->
            <g class="face-group" :style="getFaceStyle(135, 260, 'sluggish')">
              <!-- Eyes -->
              <g transform="translate(120, 260)">
                <g class="eye-container" :class="{ 'blinking': blinkingState.orange }">
                  <circle class="sclera" cx="0" cy="0" r="8" fill="white"/>
                  <g class="pupil-group" :style="getPupilStyle(120, 260, 'sluggish')">
                    <circle class="pupil" cx="0" cy="0" r="3" fill="black"/>
                  </g>
                  <rect class="eyelid" x="-10" y="-10" width="20" height="0" fill="#e17055" />
                </g>
              </g>
              <g transform="translate(150, 260)">
                <g class="eye-container" :class="{ 'blinking': blinkingState.orange }">
                  <circle class="sclera" cx="0" cy="0" r="8" fill="white"/>
                  <g class="pupil-group" :style="getPupilStyle(150, 260, 'sluggish')">
                    <circle class="pupil" cx="0" cy="0" r="3" fill="black"/>
                  </g>
                  <rect class="eyelid" x="-10" y="-10" width="20" height="0" fill="#e17055" />
                </g>
              </g>
              <!-- Mouth -->
              <path class="mouth" d="M130 280 Q135 285 140 280" stroke="#333" stroke-width="2" stroke-linecap="round" fill="none"/>
              
              <!-- Hands (Moved Inside Face Group) -->
              <g class="hands">
                <!-- Left Hand: Even Larger Paw -->
                <path class="hand" d="M105 300 C 105 255 135 255 135 300 Z" fill="#d35400" /> 
                <!-- Right Hand: Even Larger Paw -->
                <path class="hand" d="M135 300 C 135 255 165 255 165 300 Z" fill="#d35400" />
              </g>
            </g>
          </g>
        </svg>
      </div>

      <!-- Right Panel: Login Form -->
      <div class="login-content">
        <div class="login-header">
          <div class="logo-icon">
            <i class="i-tabler-chef-hat"></i>
          </div>
          <h2 class="gradient-text">家宴菜单</h2>
          <p class="subtitle">一起准备下一顿饭</p>
        </div>
        
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-item">
            <label>用户名</label>
            <div class="input-wrapper">
              <i class="i-tabler-user input-icon"></i>
              <input 
                v-model="username" 
                type="text" 
                placeholder="请输入用户名" 
                required 
                @focus="focusState = 'username'"
                @blur="focusState = 'none'"
              />
            </div>
          </div>
          
          <div class="form-item">
            <label>密码</label>
            <div class="input-wrapper">
              <i class="i-tabler-lock input-icon"></i>
              <input 
                v-model="password" 
                :type="showPassword ? 'text' : 'password'" 
                placeholder="请输入密码" 
                required 
                @focus="focusState = 'password'"
                @blur="focusState = 'none'"
              />
              <i 
                class="password-toggle"
                :class="showPassword ? 'i-tabler-eye' : 'i-tabler-eye-off'"
                @click="showPassword = !showPassword"
              ></i>
            </div>
          </div>
          
          <button :disabled="userStore.loading" class="primary-button" type="submit">
            <i v-if="!userStore.loading" class="i-tabler-login"></i>
            <i v-else class="i-tabler-loader spin"></i>
            <span>{{ userStore.loading ? '登录中...' : '进入家庭工作台' }}</span>
          </button>
        </form>
        
        <div class="tips">
          <div class="tip-item">
            <i class="i-tabler-info-circle"></i>
            <span>默认账号: admin / 123456</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/useUserStore';
import ThemeToggle from '@/components/ThemeToggle.vue';
import { useMessage } from 'naive-ui';

const userStore = useUserStore();
const username = ref('admin');
const password = ref('123456');
const showPassword = ref(false);
const router = useRouter();
const message = useMessage();

// Interaction State
type FocusState = 'none' | 'username' | 'password';
type LoginState = 'idle' | 'loading' | 'success' | 'fail';

const focusState = ref<FocusState>('none');
const loginState = ref<LoginState>('idle');
const svgRef = ref<SVGElement | null>(null);

// --- 1. Autonomic Blinking System ---
const blinkingState = reactive({
  black: false,
  purple: false,
  yellow: false,
  orange: false
});

const scheduleBlink = (char: keyof typeof blinkingState) => {
  const delay = Math.random() * 4000 + 2000; // 2s - 6s random interval
  setTimeout(() => {
    blinkingState[char] = true;
    setTimeout(() => {
      blinkingState[char] = false;
      scheduleBlink(char); // Schedule next blink
    }, 150); // Blink duration
  }, delay);
};

onMounted(() => {
  scheduleBlink('black');
  scheduleBlink('purple');
  scheduleBlink('yellow');
  scheduleBlink('orange');
  startPhysicsLoop();
});

// --- 2. Physics Inertia (Lerp) ---
const targetMouse = reactive({ x: 0, y: 0 }); // Target position (Raw mouse)
const globalMouse = reactive({ x: 0, y: 0 }); // Global normalized for body tilt

// Physics Trackers (Current Position)
const trackers = reactive({
  normal: { x: 0, y: 0 },   // Friction 0.1
  agile: { x: 0, y: 0 },    // Friction 0.2
  sluggish: { x: 0, y: 0 }  // Friction 0.05
});

let animationFrameId: number;

const lerp = (start: number, end: number, factor: number) => {
  return start + (end - start) * factor;
};

const startPhysicsLoop = () => {
  const loop = () => {
    // Update trackers
    trackers.normal.x = lerp(trackers.normal.x, targetMouse.x, 0.1);
    trackers.normal.y = lerp(trackers.normal.y, targetMouse.y, 0.1);
    
    trackers.agile.x = lerp(trackers.agile.x, targetMouse.x, 0.2);
    trackers.agile.y = lerp(trackers.agile.y, targetMouse.y, 0.2);
    
    trackers.sluggish.x = lerp(trackers.sluggish.x, targetMouse.x, 0.05);
    trackers.sluggish.y = lerp(trackers.sluggish.y, targetMouse.y, 0.05);
    
    animationFrameId = requestAnimationFrame(loop);
  };
  loop();
};

onUnmounted(() => {
  cancelAnimationFrame(animationFrameId);
});

const handleMouseMove = (event: MouseEvent) => {
  // 1. Global Offset for Body Tilt
  const centerX = window.innerWidth / 2;
  const centerY = window.innerHeight / 2;
  globalMouse.x = (event.clientX - centerX) / (window.innerWidth / 2);
  globalMouse.y = (event.clientY - centerY) / (window.innerHeight / 2);

  // 2. Local Offset for Eyes (Target for Physics)
  if (svgRef.value) {
    const rect = svgRef.value.getBoundingClientRect();
    const scaleX = 400 / rect.width;
    const scaleY = 300 / rect.height;
    targetMouse.x = (event.clientX - rect.left) * scaleX;
    targetMouse.y = (event.clientY - rect.top) * scaleY;
  }
};

// --- 3. Asynchronous Idling (Breathing) ---
const getBreathStyle = (index: number) => {
  // Randomize duration and delay
  const durations = ['3s', '4.2s', '3.5s', '5s'];
  const delays = ['-1s', '-0.5s', '-2s', '0s'];
  return {
    animationDuration: durations[index % 4],
    animationDelay: delays[index % 4]
  };
};

// --- Styles Calculation ---

// Layer 3: Body Tilt
const bodyStyle = computed(() => {
  const rotate = globalMouse.x * 5; 
  const tx = globalMouse.x * 10;
  const ty = globalMouse.y * 5;
  const scaleY = 1 + Math.abs(globalMouse.y) * 0.05;
  const skewX = globalMouse.x * 2;

  return {
    transform: `translate(${tx}px, ${ty}px) rotate(${rotate}deg) scaleY(${scaleY}) skewX(${skewX}deg)`
  };
});

// Layer 2: Face Pan (Clamped & Physics)
const getFaceStyle = (cx: number, cy: number, type: 'normal' | 'agile' | 'sluggish') => {
  const MAX_FACE_MOVE = 8;
  const tracker = trackers[type];
  
  const dx = tracker.x - cx;
  const dy = tracker.y - cy;
  
  let tx = dx / 15;
  let ty = dy / 15;
  
  tx = Math.max(-MAX_FACE_MOVE, Math.min(MAX_FACE_MOVE, tx));
  ty = Math.max(-MAX_FACE_MOVE, Math.min(MAX_FACE_MOVE, ty));
  
  return { transform: `translate(${tx}px, ${ty}px)` };
};

// Layer 1: Pupil Track (Physics)
const getPupilStyle = (cx: number, cy: number, type: 'normal' | 'agile' | 'sluggish') => {
  if (focusState.value === 'password' && !showPassword.value) {
    return { transform: `translate(0px, 0px)` }; // Force center behind hands
  }
  
  if (showPassword.value) {
     return { transform: `translate(0px, 4px)` }; 
  }

  const tracker = trackers[type];
  const dx = tracker.x - cx;
  const dy = tracker.y - cy;
  const angle = Math.atan2(dy, dx);
  const maxRadius = 4;
  const dist = Math.min(maxRadius, Math.hypot(dx, dy));
  const tx = Math.cos(angle) * dist;
  const ty = Math.sin(angle) * dist;
  
  return { transform: `translate(${tx}px, ${ty}px)` };
};

const visualStateClasses = computed(() => {
  return {
    'state-shy': focusState.value === 'password' && !showPassword.value,
    'state-peek': showPassword.value,
    'state-success': loginState.value === 'success',
    'state-fail': loginState.value === 'fail'
  };
});

const handleLogin = async () => {
  if (!username.value || !password.value) {
    message.warning('请输入用户名和密码');
    loginState.value = 'fail';
    setTimeout(() => { loginState.value = 'idle'; }, 500);
    return;
  }
  
  loginState.value = 'loading';
  
  try {
    await userStore.loginWithPassword(username.value, password.value);
    loginState.value = 'success';
    message.success('欢迎回来');
    setTimeout(() => {
      router.push('/');
    }, 1000); // Wait for success animation
  } catch (error) {
    loginState.value = 'fail';
    message.error((error as Error).message || '登录失败');
    setTimeout(() => {
      loginState.value = 'idle'; // Reset after shake
    }, 500);
  }
};
</script>

<style scoped>
.login-stage {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: var(--gradient-bg);
  padding: 20px;
}

/* Animated Background Shapes */
.orbit-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  z-index: 0;
  animation: float 10s ease-in-out infinite;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: var(--primary-color);
  top: -100px;
  left: -100px;
  opacity: 0.2;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: var(--secondary-color, #7c3aed);
  bottom: -50px;
  right: -50px;
  opacity: 0.2;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, 30px); }
}

.theme-toggle-wrapper {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 10;
}

/* Login Card */
.login-card {
  position: relative;
  width: 900px;
  max-width: 95vw;
  min-height: 550px;
  z-index: 1;
  display: flex;
  overflow: hidden;
  padding: 0;
  border-radius: 24px;
}

/* Left Panel */
.login-visual {
  width: 45%;
  background: #f5f5f7;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

:global(.dark) .login-visual {
  background: #2d2d30;
}

#login-characters {
  width: 85%;
  height: auto;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.character {
  /* Layer 3: Body Tilt & Stretch */
  transform-box: fill-box;
  transform-origin: center bottom;
  transition: transform 0.2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.body-shape {
  /* Organic Breathing */
  transform-origin: center;
  animation: breathe 4s ease-in-out infinite;
}

@keyframes breathe {
  0%, 100% { transform: scale(1, 1); }
  50% { transform: scale(1.02, 0.98); }
}

.face-group {
  /* Layer 2: Face Pan (Clamped) */
  transition: transform 0.15s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.pupil-group {
  /* Layer 1: Pupil Track */
  /* Remove transition here because JS Lerp handles smoothing */
  /* transition: transform 0.1s linear; */ 
}

.eyelid {
  height: 0; /* Default open */
  transition: height 0.15s ease-in-out; /* Faster blink */
}

.mouth {
  transition: d 0.3s ease;
}

/* Hands for Orange */
.hands {
  transition: transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275); /* Back-out bounce */
  transform: translateY(100px); /* Hidden at bottom */
}

.hand {
  /* Hand shape styling */
}

/* --- STATE: BLINKING --- */
.blinking .eyelid {
  height: 24px; /* Close eye */
}

/* --- STATE: SHY (Password Focus) --- */
.state-shy .pupil-group {
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); /* Slower for shy */
}

/* Purple: Look up (Reduced to prevent separation) */
.state-shy #char-purple .pupil-group {
  transform: translateY(-4px) !important;
}

/* All Characters: Close eyes by default in Shy */
.state-shy .eyelid {
  height: 24px; /* Fully cover eye */
}

/* Orange: KEEP EYES OPEN, but Hands Cover */
.state-shy #char-orange .eyelid {
  height: 0 !important; /* Force open */
}

.state-shy .hands {
  transform: translateY(-25px); /* Pop up to cover eyes (Adjusted: -25px covers bottom of eyes) */
}

/* Mouths: Shy */
.state-shy .mouth {
  d: path("M 0 0"); /* Hide mouths or make small line */
  opacity: 0;
}

/* --- STATE: PEEK (Show Password) --- */
/* Specific Peek Mouths */
.state-peek #char-purple .mouth {
  d: path("M 115 145 A 5 5 0 1 1 125 145 A 5 5 0 1 1 115 145"); /* Circle */
}
.state-peek #char-black .mouth {
  d: path("M 195 110 A 3 3 0 1 1 205 110 A 3 3 0 1 1 195 110");
}
.state-peek #char-orange .mouth {
  d: path("M 130 280 A 3 3 0 1 1 140 280 A 3 3 0 1 1 130 280");
}
.state-peek #char-yellow .mouth {
  d: path("M 225 285 A 3 3 0 1 1 235 285 A 3 3 0 1 1 225 285");
}


/* --- STATE: SUCCESS (Login Success) --- */
.state-success .character {
  animation: bounce 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) infinite;
}

.state-success #char-purple { animation-delay: 0.1s; }
.state-success #char-yellow { animation-delay: 0.2s; }
.state-success #char-black { animation-delay: 0.05s; }

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-30px); }
}

/* --- STATE: FAIL (Login Fail) --- */
.shake-animation {
  animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both;
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}

/* Right Panel */
.login-content {
  width: 55%;
  background: var(--bg-card);
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 24px;
  border-radius: 16px;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: white;
  box-shadow: var(--shadow-glow);
}

.login-header h2 {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 14px;
  color: var(--text-tertiary);
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-left: 4px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  z-index: 5; /* Ensure inputs are clickable */
}

.input-icon {
  position: absolute;
  left: 16px;
  color: var(--text-tertiary);
  font-size: 18px;
  transition: color 0.3s;
  pointer-events: none;
}

.password-toggle {
  position: absolute;
  right: 16px;
  color: var(--text-tertiary);
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;
  z-index: 2;
}

.password-toggle:hover {
  color: var(--text-primary);
}

input {
  width: 100%;
  padding: 12px 44px 12px 44px;
  border-radius: 12px;
  border: 1px solid var(--border-primary);
  background: var(--bg-body);
  color: var(--text-primary);
  font-size: 15px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

input:hover {
  border-color: var(--text-tertiary);
}

input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--border-focus);
  background: var(--bg-card);
}

.input-wrapper:focus-within .input-icon {
  color: var(--primary-color);
}

.primary-button {
  margin-top: 8px;
  padding: 14px;
  border-radius: 12px;
  border: none;
  background: var(--gradient-primary);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: var(--shadow-glow);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.primary-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  filter: brightness(1.1);
}

.primary-button:active:not(:disabled) {
  transform: translateY(0);
}

.primary-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.tips {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-secondary);
  display: flex;
  justify-content: center;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-tertiary);
  background: var(--bg-body);
  padding: 6px 12px;
  border-radius: 20px;
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
    width: 100%;
    margin: 16px;
    max-width: 400px;
  }

  .login-visual {
    width: 100%;
    height: 180px;
    padding: 20px;
  }
  
  #login-characters {
    width: auto;
    height: 100%;
  }

  .login-content {
    width: 100%;
    padding: 32px 24px;
  }
}
</style>
