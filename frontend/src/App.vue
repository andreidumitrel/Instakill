<template>
  <div class="app-shell">
    <header class="app-header">
      <h1 class="logo">Instakill</h1>
      <nav v-if="user">
        <span class="user">@{{ user.username }}</span>
        <button class="ghost" @click="logout">Logout</button>
      </nav>
    </header>

    <main class="content">
      <section v-if="!user" class="card auth-card">
        <div class="tabs">
          <button :class="{ active: authMode === 'login' }" @click="authMode = 'login'">Login</button>
          <button :class="{ active: authMode === 'register' }" @click="authMode = 'register'">Register</button>
        </div>
        <form @submit.prevent="submitAuth" class="form">
          <input v-model="authForm.usernameOrEmail" v-if="authMode === 'login'" placeholder="Username or email" required>
          <input v-model="authForm.username" v-else placeholder="Username" required minlength="3">
          <input v-model="authForm.email" v-if="authMode === 'register'" type="email" placeholder="Email" required>
          <input v-model="authForm.password" type="password" placeholder="Password" required minlength="6">
          <p v-if="error" class="error">{{ error }}</p>
          <button type="submit" class="primary">Continue</button>
        </form>
      </section>

      <section v-else class="feed">
        <form @submit.prevent="createNewPost" class="card form composer">
          <textarea v-model="composer.content" rows="3" placeholder="Share something..." required></textarea>

          <div class="media-upload-row">
            <label class="file-btn">
              <span>+ Add image or video</span>
              <input type="file" @change="onFileSelected" accept="image/*,video/*" :disabled="uploading" style="display:none">
            </label>
            <span v-if="uploading" class="uploading-text">Uploading...</span>
            <span v-if="uploadedFilename" class="uploaded-badge">✓ {{ uploadedFilename }}</span>
            <span v-if="uploadError" class="error">{{ uploadError }}</span>
          </div>

          <input v-model="composer.imageUrl" placeholder="Media URL (or paste external link)">

          <div v-if="composer.imageUrl" class="composer-media-preview">
            <video v-if="isVideo(composer.imageUrl)" :src="composer.imageUrl" controls class="preview-media"></video>
            <img v-else :src="composer.imageUrl" alt="Preview" class="preview-media">
          </div>

          <div class="actions">
            <button type="submit" class="primary" :disabled="uploading">Post</button>
          </div>
        </form>

        <div class="feed-toolbar">
          <h2>Feed</h2>
          <div class="sort">
            <button :class="{ active: sort === 'new' }" @click="changeSort('new')">Newest</button>
            <button :class="{ active: sort === 'trending' }" @click="changeSort('trending')">Trending</button>
          </div>
        </div>

        <article v-for="post in posts" :key="post.id" class="card post">
          <header>
            <div class="author">
              <div class="avatar">{{ post.author.username[0] }}</div>
              <div>
                <h3>@{{ post.author.username }}</h3>
                <small>{{ formatDate(post.createdAt) }}</small>
              </div>
            </div>
            <div class="metrics">
              <span>{{ post.likeCount }} likes</span>
              <span>{{ post.commentCount }} comments</span>
            </div>
          </header>
          <p class="content-text">{{ post.content }}</p>
          <div v-if="post.imageUrl" class="post-media-box">
            <video v-if="isVideo(post.imageUrl)" :src="post.imageUrl" controls class="post-media"></video>
            <img v-else :src="post.imageUrl" alt="post image" class="post-media">
          </div>

          <!-- Comments Section -->
          <section class="comments">
            <div v-if="comments[post.id] && comments[post.id].length">
              <div v-for="comment in comments[post.id]" :key="comment.id" class="comment">
                <b>@{{ comment.author.username }}</b> <small>{{ formatDate(comment.createdAt) }}</small>
                <div>{{ comment.content }}</div>
              </div>
            </div>
            <div v-else class="no-comments">
              <em>No comments yet.</em>
            </div>
            <form v-if="user" @submit.prevent="submitComment(post.id)" class="comment-form">
              <input
                v-model="newComment[post.id]"
                :placeholder="'Add a comment...'"
                required
                minlength="1"
              />
              <button type="submit" :disabled="commentLoading[post.id]">Comment</button>
            </form>
            <div v-if="commentError[post.id]" class="error">{{ commentError[post.id] }}</div>
          </section>

          <footer>
            <button class="like-btn" :class="{ liked: post.likedByCurrentUser }" @click="togglePostLike(post)">
              {{ post.likedByCurrentUser ? '♥ Liked' : '♡ Like' }}
            </button>
          </footer>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { createPost, fetchMe, listFeed, login, register as registerUser, toggleLike, listComments, addComment, uploadMedia } from './api'

const user = ref(null)
const posts = ref([])
const sort = ref('new')
const authMode = ref('login')
const error = ref('')
const authForm = reactive({ username: '', email: '', password: '', usernameOrEmail: '' })
const composer = reactive({ content: '', imageUrl: '' })
const uploading = ref(false)
const uploadError = ref('')
const uploadedFilename = ref('')

function isVideo(url) {
  if (!url) return false
  const clean = url.split('?')[0].toLowerCase()
  return clean.endsWith('.mp4') || clean.endsWith('.mov') || clean.endsWith('.webm') || clean.endsWith('.avi') || clean.endsWith('.mkv')
}

async function onFileSelected(event) {
  const file = event.target.files?.[0]
  if (!file) return
  uploading.value = true
  uploadError.value = ''
  uploadedFilename.value = ''
  try {
    const res = await uploadMedia(file)
    composer.imageUrl = res.mediaUrl
    uploadedFilename.value = file.name
  } catch (e) {
    uploadError.value = e.response?.data?.detail || e.response?.data?.message || 'Failed to upload media'
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

// Comments state
const comments = reactive({})
const newComment = reactive({})
const commentLoading = reactive({})
const commentError = reactive({})

onMounted(async () => {
  await bootstrap()
})

async function bootstrap() {
  try {
    user.value = await fetchMe()
    await loadFeed()
  } catch (e) {
    user.value = null
  }
}

async function submitAuth() {
  error.value = ''
  try {
    if (authMode.value === 'login') {
      const result = await login({
        usernameOrEmail: authForm.usernameOrEmail,
        password: authForm.password
      })
      user.value = result.user
    } else {
      const result = await registerUser({
        username: authForm.username,
        email: authForm.email,
        password: authForm.password
      })
      user.value = result.user
    }
    await loadFeed()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Authentication failed'
  }
}

async function loadFeed() {
  posts.value = await listFeed({ sort: sort.value })
  // Load comments for each post
  for (const post of posts.value) {
    await loadComments(post.id)
  }
}

async function loadComments(postId) {
  try {
    commentError[postId] = ''
    comments[postId] = await listComments(postId)
  } catch (e) {
    comments[postId] = []
    commentError[postId] = 'Failed to load comments'
  }
}

async function submitComment(postId) {
  if (!newComment[postId] || !newComment[postId].trim()) return
  commentLoading[postId] = true
  commentError[postId] = ''
  try {
    await addComment(postId, { content: newComment[postId] })
    newComment[postId] = ''
    await loadComments(postId)
    // Optionally, update commentCount in post
    const post = posts.value.find(p => p.id === postId)
    if (post) post.commentCount = (comments[postId]?.length || 0)
  } catch (e) {
    commentError[postId] = e.response?.data?.detail || 'Failed to add comment'
  } finally {
    commentLoading[postId] = false
  }
}

async function createNewPost() {
  if (!composer.content.trim()) return
  await createPost({ content: composer.content, imageUrl: composer.imageUrl || null })
  composer.content = ''
  composer.imageUrl = ''
  uploadedFilename.value = ''
  await loadFeed()
}

async function togglePostLike(post) {
  const result = await toggleLike(post.id)
  post.likedByCurrentUser = result.liked
  post.likeCount = result.likeCount
}

function changeSort(next) {
  if (sort.value === next) return
  sort.value = next
  loadFeed()
}

function formatDate(iso) {
  return new Date(iso).toLocaleString()
}

async function logout() {
  try {
    await fetch('/auth/logout', { method: 'POST', credentials: 'include' })
  } catch (e) {
    // ignore
  }
  document.cookie = 'ACCESS_TOKEN=; Max-Age=0; path=/'
  user.value = null
  posts.value = []
}
</script>

<style scoped>
.app-shell {
  --ink: #111827;
  --muted: #6b7280;
  --line: #e5e7eb;
  --surface: #ffffff;
  --accent: #dd2a7b;
  --accent-grad: linear-gradient(135deg, #f58529 0%, #dd2a7b 50%, #8134af 100%);

  max-width: 640px;
  margin: 0 auto;
  padding: 2.5rem 1rem 4rem;
  color: var(--ink);
  font-family: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
  line-height: 1.5;
}

/* ---------- Header ---------- */
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--line);
}

.logo {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  background: var(--accent-grad);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.app-header nav {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user {
  font-weight: 600;
  color: var(--muted);
}

/* ---------- Layout ---------- */
.content {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.feed {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 20px;
  padding: 1.25rem 1.5rem;
  box-shadow: 0 1px 2px rgba(17, 24, 39, 0.04), 0 8px 24px rgba(17, 24, 39, 0.06);
}

.auth-card {
  max-width: 420px;
  width: 100%;
  margin: 2rem auto 0;
}

/* ---------- Forms ---------- */
.form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.form input,
.form textarea,
.comment-form input,
.composer input {
  width: 100%;
  padding: 0.75rem 0.9rem;
  border-radius: 12px;
  border: 1px solid #d1d5db;
  background: #fafafa;
  font: inherit;
  color: var(--ink);
  transition: border-color 0.15s, box-shadow 0.15s, background 0.15s;
}

.form textarea {
  resize: vertical;
}

.form input:focus,
.form textarea:focus,
.comment-form input:focus,
.composer input:focus {
  outline: none;
  background: #fff;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(221, 42, 123, 0.15);
}

button {
  font: inherit;
  cursor: pointer;
  transition: transform 0.1s, opacity 0.15s, background 0.15s;
}

button:active {
  transform: scale(0.97);
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

button.primary,
.form button,
.comment-form button {
  background: var(--accent-grad);
  color: #fff;
  border: none;
  padding: 0.6rem 1.4rem;
  border-radius: 999px;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(221, 42, 123, 0.25);
}

button.primary {
  align-self: flex-end;
}

button.ghost {
  background: transparent;
  color: var(--ink);
  border: 1px solid #d1d5db;
  padding: 0.45rem 1rem;
  border-radius: 999px;
  font-weight: 600;
}

button.ghost:hover {
  background: #f3f4f6;
}

.tabs {
  display: flex;
  gap: 1.25rem;
  margin-bottom: 1.25rem;
  border-bottom: 1px solid var(--line);
}

.tabs button {
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  padding: 0.5rem 0;
  font-weight: 600;
  color: var(--muted);
}

.tabs button.active {
  color: var(--ink);
  border-bottom-color: var(--accent);
}

.error {
  color: #dc2626;
  font-size: 0.9rem;
}

/* ---------- Composer ---------- */
.composer {
  padding: 1.25rem;
}

.composer textarea {
  font-size: 1rem;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.media-upload-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.file-btn {
  display: inline-block;
  background: #f3f4f6;
  color: var(--ink);
  padding: 0.45rem 0.9rem;
  border: 1px dashed #cbd5e1;
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  transition: background 0.15s, border-color 0.15s;
}

.file-btn:hover {
  background: #e5e7eb;
  border-color: var(--accent);
}

.uploading-text {
  font-size: 0.85rem;
  color: #2563eb;
}

.uploaded-badge {
  font-size: 0.85rem;
  color: #059669;
  font-weight: 600;
}

.composer-media-preview {
  margin: 0.25rem 0;
}

.preview-media {
  max-width: 100%;
  max-height: 180px;
  border-radius: 12px;
  display: block;
}

/* ---------- Feed toolbar ---------- */
.feed-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.5rem;
}

.feed-toolbar h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
}

.sort {
  display: flex;
  gap: 0.25rem;
  background: #f3f4f6;
  padding: 0.25rem;
  border-radius: 999px;
}

.sort button {
  border: none;
  background: transparent;
  padding: 0.35rem 0.9rem;
  border-radius: 999px;
  font-weight: 600;
  color: var(--muted);
}

.sort button.active {
  background: #fff;
  color: var(--ink);
  box-shadow: 0 1px 3px rgba(17, 24, 39, 0.12);
}

/* ---------- Post ---------- */
.post header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.author {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--accent-grad);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  text-transform: uppercase;
  flex-shrink: 0;
}

.post h3 {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 700;
}

.post small {
  color: var(--muted);
  font-size: 0.8rem;
}

.metrics {
  display: flex;
  gap: 0.75rem;
  font-size: 0.8rem;
  color: var(--muted);
  white-space: nowrap;
}

.content-text {
  margin: 0 0 0.75rem;
  white-space: pre-wrap;
  font-size: 1rem;
}

.post-media-box {
  width: 100%;
  aspect-ratio: 4 / 3;
  margin: 0.5rem 0 0.25rem;
  border-radius: 14px;
  overflow: hidden;
  background: #f3f4f6;
}

.post-media {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post footer {
  margin-top: 1rem;
}

.like-btn {
  background: transparent;
  border: 1px solid #d1d5db;
  color: var(--ink);
  padding: 0.4rem 1rem;
  border-radius: 999px;
  font-weight: 600;
}

.like-btn:hover {
  background: #f9fafb;
}

.like-btn.liked {
  color: var(--accent);
  border-color: rgba(221, 42, 123, 0.4);
  background: rgba(221, 42, 123, 0.06);
}

/* ---------- Comments ---------- */
.comments {
  margin-top: 1rem;
  padding-top: 0.9rem;
  border-top: 1px solid var(--line);
}

.comment {
  margin-bottom: 0.65rem;
  font-size: 0.95rem;
}

.comment small {
  color: var(--muted);
  font-size: 0.75rem;
}

.no-comments {
  color: var(--muted);
  font-size: 0.9rem;
  margin-bottom: 0.65rem;
}

.comment-form {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.comment-form input {
  flex: 1;
  padding: 0.5rem 0.8rem;
  font-size: 0.9rem;
}

.comment-form button {
  padding: 0.45rem 1.1rem;
  font-size: 0.9rem;
  box-shadow: none;
}

@media (max-width: 480px) {
  .app-shell {
    padding: 1.5rem 0.75rem 3rem;
  }

  .card {
    padding: 1rem;
  }

  .post header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
