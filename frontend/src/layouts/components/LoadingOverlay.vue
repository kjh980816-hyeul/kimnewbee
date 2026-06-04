<script setup lang="ts">
import { onMounted, ref } from 'vue';

// 앱 최초 진입 시 한 번 보여주는 로딩 오버레이(시안 재현). 짧게 떴다가 페이드아웃.
const done = ref(false);
const hidden = ref(false);

interface Star {
  top: string;
  left: string;
  size: string;
  green: boolean;
  dur: string;
  delay: string;
  min: string;
  max: string;
}

// 별밭 — setup에서 한 번 생성(고정).
const stars: Star[] = Array.from({ length: 46 }, (_, i) => {
  const r = (n: number) => ((Math.sin(i * 99.7 + n) + 1) / 2);
  return {
    top: `${(r(1) * 100).toFixed(2)}%`,
    left: `${(r(2) * 100).toFixed(2)}%`,
    size: `${(1 + r(3) * 2).toFixed(1)}px`,
    green: r(4) > 0.78,
    dur: `${(2 + r(5) * 3).toFixed(1)}s`,
    delay: `${(r(6) * 3).toFixed(1)}s`,
    min: (0.1 + r(7) * 0.2).toFixed(2),
    max: (0.7 + r(8) * 0.3).toFixed(2),
  };
});

onMounted(() => {
  window.setTimeout(() => {
    done.value = true;
  }, 1500);
  window.setTimeout(() => {
    hidden.value = true;
  }, 2600);
});
</script>

<template>
  <div v-if="!hidden" class="loader" :class="{ done }">
    <div class="stars">
      <span
        v-for="(s, i) in stars"
        :key="i"
        class="star"
        :class="{ g: s.green }"
        :style="{
          top: s.top,
          left: s.left,
          width: s.size,
          height: s.size,
          '--dur': s.dur,
          '--delay': s.delay,
          '--min': s.min,
          '--max': s.max,
        }"
      ></span>
    </div>
    <div class="comet" style="--cd: 0s"></div>
    <div class="comet" style="--cd: 3.4s; left: 20%"></div>

    <svg
      v-for="pos in ['tl', 'tr', 'bl', 'br']"
      :key="pos"
      class="frame"
      :class="pos"
      viewBox="0 0 120 120"
      fill="none"
      stroke="currentColor"
      stroke-width="1.3"
    >
      <path d="M8 8 H64 M8 8 V64" stroke-width="1.6"></path>
      <path d="M8 22 H50 M22 8 V50" opacity=".6"></path>
      <circle cx="8" cy="8" r="3.5"></circle>
      <path d="M16 16 L34 16 L16 34 Z" opacity=".5" fill="currentColor" stroke="none"></path>
      <path d="M58 8 a50 50 0 0 1 -50 50" opacity=".35" stroke-dasharray="2 4"></path>
    </svg>
    <div class="frame-inner"></div>

    <div class="emblem">
      <svg class="ring r1" viewBox="0 0 240 240" fill="none" stroke="#5FC76B">
        <circle cx="120" cy="120" r="116" stroke-width="1" opacity=".5"></circle>
        <circle cx="120" cy="120" r="116" stroke-width="1" stroke-dasharray="1 9" opacity=".9"></circle>
      </svg>
      <svg class="ring r2" viewBox="0 0 240 240" fill="none" stroke="#A3EC8E">
        <circle cx="120" cy="120" r="96" stroke-width="1" stroke-dasharray="3 7" opacity=".55"></circle>
        <g stroke-width="1.4" opacity=".8">
          <path d="M120 14 V30"></path><path d="M120 210 V226"></path>
          <path d="M14 120 H30"></path><path d="M210 120 H226"></path>
        </g>
      </svg>
      <svg class="ring r3" viewBox="0 0 240 240" fill="none" stroke="#5FC76B">
        <circle cx="120" cy="120" r="78" stroke-width="1" opacity=".35"></circle>
        <g opacity=".7">
          <circle cx="120" cy="42" r="2" fill="#A3EC8E" stroke="none"></circle>
          <circle cx="120" cy="198" r="2" fill="#A3EC8E" stroke="none"></circle>
          <circle cx="42" cy="120" r="2" fill="#A3EC8E" stroke="none"></circle>
          <circle cx="198" cy="120" r="2" fill="#A3EC8E" stroke="none"></circle>
        </g>
      </svg>
      <svg class="pepper-emblem" viewBox="0 0 100 100" fill="none">
        <path d="M40 22 Q56 14 64 24 Q60 27 60 32" stroke="#A3EC8E" stroke-width="2.2" stroke-linecap="round"></path>
        <path d="M38 30 Q26 46 34 70 Q46 90 66 76 Q82 62 76 38 Q70 26 54 28 Q44 28 38 30 Z" stroke="#5FC76B" stroke-width="2.4" stroke-linejoin="round"></path>
        <path d="M46 40 Q40 56 48 72" stroke="#A3EC8E" stroke-width="1.8" stroke-linecap="round" opacity=".7"></path>
        <path d="M58 38 Q56 54 62 68" stroke="#A3EC8E" stroke-width="1.4" stroke-linecap="round" opacity=".45"></path>
      </svg>
    </div>

    <div class="loading-text">Now Loading</div>
    <div class="loading-sub">초록고추밭에 들어가는 중…</div>
    <div class="loading-bar"><i></i></div>
  </div>
</template>

<style scoped>
.loader {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: radial-gradient(120% 90% at 50% 38%, #161618 0%, #0f0f10 46%, #060606 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: opacity 1s var(--ease), visibility 1s var(--ease);
}
.loader.done {
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
}

.stars {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.star {
  position: absolute;
  border-radius: 50%;
  background: #fff;
  animation: twinkle var(--dur, 3s) ease-in-out infinite;
  animation-delay: var(--delay, 0s);
}
.star.g {
  background: var(--green-bright);
  box-shadow: 0 0 6px var(--green-glow);
}
@keyframes twinkle {
  0%,
  100% {
    opacity: var(--min, 0.15);
    transform: scale(0.8);
  }
  50% {
    opacity: var(--max, 0.9);
    transform: scale(1.15);
  }
}

.comet {
  position: absolute;
  top: -10%;
  left: -10%;
  width: 2px;
  height: 160px;
  background: linear-gradient(to bottom, rgba(163, 236, 142, 0) 0%, rgba(163, 236, 142, 0.9) 60%, #fff 100%);
  border-radius: 2px;
  filter: drop-shadow(0 0 6px var(--green-glow));
  transform: rotate(38deg);
  opacity: 0;
  animation: comet 7s linear infinite;
  animation-delay: var(--cd, 0s);
}
.comet::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: -2px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 10px 2px var(--green-bright);
}
@keyframes comet {
  0% {
    opacity: 0;
    transform: translate(-20vw, -20vh) rotate(38deg);
  }
  8% {
    opacity: 1;
  }
  22% {
    opacity: 1;
  }
  35%,
  100% {
    opacity: 0;
    transform: translate(120vw, 120vh) rotate(38deg);
  }
}

.frame {
  position: absolute;
  width: 120px;
  height: 120px;
  color: var(--green);
  opacity: 0.55;
}
.frame.tl {
  top: 34px;
  left: 34px;
}
.frame.tr {
  top: 34px;
  right: 34px;
  transform: scaleX(-1);
}
.frame.bl {
  bottom: 34px;
  left: 34px;
  transform: scaleY(-1);
}
.frame.br {
  bottom: 34px;
  right: 34px;
  transform: scale(-1, -1);
}
.frame-inner {
  position: absolute;
  inset: 60px;
  border: 1px dotted rgba(95, 199, 107, 0.22);
  border-radius: 6px;
  pointer-events: none;
}

.emblem {
  position: relative;
  width: 240px;
  height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: emblem-in 1.4s var(--ease-out) both;
}
@keyframes emblem-in {
  from {
    opacity: 0;
    transform: scale(0.86);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
.ring {
  position: absolute;
  inset: 0;
}
.ring.r1 {
  animation: spin 26s linear infinite;
}
.ring.r2 {
  animation: spin 18s linear infinite reverse;
}
.ring.r3 {
  animation: spin 40s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
.pepper-emblem {
  width: 96px;
  height: 96px;
  animation: float 5s ease-in-out infinite;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
}

.loading-text {
  margin-top: 54px;
  font-family: var(--font-serif);
  font-size: 15px;
  letter-spacing: 0.42em;
  text-indent: 0.42em;
  color: var(--green-bright);
  text-transform: uppercase;
  font-weight: 400;
  white-space: nowrap;
  animation: textpulse 2.6s ease-in-out infinite;
}
@keyframes textpulse {
  0%,
  100% {
    opacity: 0.4;
  }
  50% {
    opacity: 1;
  }
}
.loading-sub {
  margin-top: 14px;
  font-family: var(--font-hand);
  font-size: 15px;
  color: var(--text-mute);
  letter-spacing: 0.04em;
}
.loading-bar {
  margin-top: 22px;
  width: 180px;
  height: 2px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 2px;
  overflow: hidden;
}
.loading-bar > i {
  display: block;
  height: 100%;
  width: 40%;
  background: linear-gradient(90deg, transparent, var(--green), var(--green-bright));
  border-radius: 2px;
  animation: sweep 1.6s var(--ease) infinite;
}
@keyframes sweep {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(350%);
  }
}

.loader.done .emblem {
  animation: scatter 0.9s var(--ease) forwards;
}
@keyframes scatter {
  to {
    opacity: 0;
    transform: scale(1.5);
    filter: blur(8px);
  }
}
.loader.done .loading-text,
.loader.done .loading-sub,
.loader.done .loading-bar {
  opacity: 0;
  transition: opacity 0.4s;
}

@media (prefers-reduced-motion: reduce) {
  .star,
  .comet,
  .ring,
  .pepper-emblem,
  .loading-bar > i {
    animation: none;
  }
  .star {
    opacity: 0.5;
  }
}
</style>
