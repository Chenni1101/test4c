<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = withDefaults(
  defineProps<{ poster?: string; modelUrl?: string; modelLabel?: string }>(),
  { poster: '/exhibits/44c6dedd234af2dade7c7bf2fc3b1383.jpg', modelUrl: '', modelLabel: '藏品三维模型' }
)

const host = ref<HTMLElement | null>(null)
const viewer = ref<any>(null)
const ready = ref(false)
const fallback = ref(false)
const loading = ref(false)
const modelSrc = computed(() => props.modelUrl || import.meta.env.VITE_3D_MODEL_A || '')
let observer: IntersectionObserver | undefined
let timer = 0

const isMobile = () => window.matchMedia('(max-width: 767px), (prefers-reduced-data: reduce)').matches
function reset() { if (viewer.value) { viewer.value.cameraOrbit = '0deg 75deg 105%'; viewer.value.fieldOfView = '30deg' } }
function rotate() { if (viewer.value) viewer.value.cameraOrbit = '45deg 75deg 105%' }
async function mountViewer() {
  if (!modelSrc.value || isMobile()) { fallback.value = true; return }
  loading.value = true
  try {
    if (!customElements.get('model-viewer')) await import('@google/model-viewer')
    ready.value = true
    timer = window.setTimeout(() => { if (loading.value) { fallback.value = true; loading.value = false } }, 12_000)
  } catch { fallback.value = true } finally { if (fallback.value) loading.value = false }
}
function onLoad() { loading.value = false; window.clearTimeout(timer) }
function onError() { fallback.value = true; loading.value = false; window.clearTimeout(timer) }
onMounted(() => { observer = new IntersectionObserver((items) => { if (items.some((item) => item.isIntersecting)) { mountViewer(); observer?.disconnect() } }, { rootMargin: '180px' }); if (host.value) observer.observe(host.value) })
onBeforeUnmount(() => { observer?.disconnect(); window.clearTimeout(timer) })
</script>

<template>
  <section ref="host" class="viewer">
    <div v-if="fallback" class="fallback">
      <img :src="poster" alt="藏品预览" loading="lazy" />
      <p>当前资产未附加可加载的 GLB/GLTF 模型，已展示藏品预览图。</p>
    </div>
    <template v-else>
      <a-spin :spinning="loading">
        <model-viewer v-if="ready" ref="viewer" :src="modelSrc" :alt="modelLabel" :poster="poster" camera-controls touch-action="pan-y" shadow-intensity="0.8" exposure="1" @load="onLoad" @error="onError" />
      </a-spin>
      <a-space class="controls" wrap><a-button @click="rotate">旋转</a-button><a-button @click="reset">重置视角</a-button></a-space>
    </template>
  </section>
</template>

<style scoped>
.viewer { min-height: 280px; }
model-viewer { width: 100%; height: 360px; background: #f1f5f9; border-radius: 12px; }
.controls { margin-top: 12px; }
.fallback { border: 1px dashed #94a3b8; border-radius: 12px; padding: 12px; text-align: center; background: #f8fafc; }
.fallback img { max-width: 100%; max-height: 280px; object-fit: contain; }
@media (max-width: 767px) { model-viewer { height: 260px; } }
</style>
