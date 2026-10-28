import os
import matplotlib.pyplot as plt
import matplotlib.patches as patches
from matplotlib.backends.backend_pdf import PdfPages

OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "docs", "architecture")
os.makedirs(OUT_DIR, exist_ok=True)

# Styling constants
COLOR_PERSON = "#08427B"
COLOR_SYSTEM = "#1168BD"
COLOR_CONTAINER = "#438DD5"
COLOR_COMPONENT = "#3B82F6"
COLOR_DATABASE = "#1E3A8A"
COLOR_EXTERNAL = "#6B7280"
COLOR_BG = "#F8FAFC"
COLOR_TEXT_LIGHT = "#FFFFFF"
COLOR_TEXT_MUTED = "#E2E8F0"
COLOR_TEXT_DARK = "#0F172A"
COLOR_BORDER = "#CBD5E1"
COLOR_ARROW = "#475569"


def draw_box(ax, x, y, w, h, title, subtitle, desc, bg_color, text_color=COLOR_TEXT_LIGHT, is_db=False):
    """Draw a styled C4 element box."""
    if is_db:
        # Draw cylinder base representation
        rect = patches.FancyBboxPatch(
            (x, y), w, h,
            boxstyle="round,pad=0.02,rounding_size=0.15",
            facecolor=bg_color, edgecolor="#1E293B", linewidth=1.5
        )
        ax.add_patch(rect)
    else:
        rect = patches.FancyBboxPatch(
            (x, y), w, h,
            boxstyle="round,pad=0.02,rounding_size=0.08",
            facecolor=bg_color, edgecolor="#1E293B", linewidth=1.2
        )
        ax.add_patch(rect)

    # Title & type
    ax.text(x + w / 2, y + h * 0.78, title, ha="center", va="center",
            fontsize=11, fontweight="bold", color=text_color)
    if subtitle:
        ax.text(x + w / 2, y + h * 0.60, f"[{subtitle}]", ha="center", va="center",
                fontsize=8.5, fontstyle="italic", color=COLOR_TEXT_MUTED if text_color == COLOR_TEXT_LIGHT else "#64748B")
    if desc:
        ax.text(x + w / 2, y + h * 0.28, desc, ha="center", va="center",
                fontsize=8, color=COLOR_TEXT_MUTED if text_color == COLOR_TEXT_LIGHT else "#334155",
                wrap=True, multialignment="center")


def draw_arrow(ax, x1, y1, x2, y2, label=""):
    """Draw a relationship arrow with label."""
    ax.annotate(
        "", xy=(x2, y2), xytext=(x1, y1),
        arrowprops=dict(arrowstyle="-|>", color=COLOR_ARROW, lw=1.5, ls="--", mutation_scale=14)
    )
    if label:
        mx = (x1 + x2) / 2
        my = (y1 + y2) / 2
        ax.text(mx, my + 0.02, label, ha="center", va="bottom",
                fontsize=7.5, color="#1E293B",
                bbox=dict(boxstyle="square,pad=0.15", facecolor="#FFFFFF", edgecolor="#E2E8F0", alpha=0.9))


def create_context_diagram():
    fig, ax = plt.subplots(figsize=(14, 8), dpi=300)
    fig.patch.set_facecolor(COLOR_BG)
    ax.set_facecolor(COLOR_BG)
    ax.set_xlim(0, 1.4)
    ax.set_ylim(0, 1.0)
    ax.axis("off")

    # Header
    ax.text(0.7, 0.96, "Instakill — System Context Diagram (C4 Level 1)",
            ha="center", va="center", fontsize=15, fontweight="bold", color=COLOR_TEXT_DARK)
    ax.text(0.7, 0.92, "High-level view of users, external actors, and the Instakill system",
            ha="center", va="center", fontsize=9.5, color="#64748B")

    # Person: Registered User
    draw_box(ax, 0.08, 0.62, 0.26, 0.22,
             "Registered User", "Person",
             "A registered social network user who\nbrowses the feed, publishes posts,\nand likes or comments on posts.",
             COLOR_PERSON)

    # Person: Anonymous User
    draw_box(ax, 0.08, 0.35, 0.26, 0.22,
             "Anonymous User", "Person",
             "A visitor exploring public feeds,\nlogin, or registering a new account.",
             COLOR_PERSON)

    # Person: Developer / Admin
    draw_box(ax, 0.08, 0.08, 0.26, 0.22,
             "Admin / Developer", "Person",
             "Inspects health metrics, OpenAPI\nendpoints, and manages development data.",
             COLOR_PERSON)

    # System: Instakill Monolith
    draw_box(ax, 0.52, 0.28, 0.38, 0.44,
             "Instakill System", "Software System (Monolith)",
             "Provides social networking capabilities:\n• User authentication & session cookies (JWT)\n• Chronological & Trending feed algorithms\n• Post creation, like counters & comments\n• SQLite persistence and REST / MVC APIs",
             COLOR_SYSTEM)

    # External System: Image CDN
    draw_box(ax, 1.05, 0.40, 0.26, 0.24,
             "External Image CDN / Host", "External System",
             "Third-party image hosting or CDN\nproviding remote images embedded\nby URL in user posts.",
             COLOR_EXTERNAL)

    # Arrows
    draw_arrow(ax, 0.34, 0.72, 0.52, 0.58, "Creates posts, likes, comments, views feed\n[HTTPS / HTML & JSON]")
    draw_arrow(ax, 0.34, 0.45, 0.52, 0.48, "Views public feed, signs up / logs in\n[HTTPS / HTML & JSON]")
    draw_arrow(ax, 0.34, 0.20, 0.52, 0.38, "Monitors health & Swagger UI\n[HTTP :8080/actuator, /swagger]")
    draw_arrow(ax, 0.90, 0.52, 1.05, 0.52, "Embeds / resolves image URLs\n[HTTPS]")

    plt.tight_layout()
    png_path = os.path.join(OUT_DIR, "c4_context.png")
    pdf_path = os.path.join(OUT_DIR, "c4_context.pdf")
    plt.savefig(png_path, dpi=300, bbox_inches="tight")
    plt.savefig(pdf_path, bbox_inches="tight")
    plt.close()
    print(f"Context diagram saved to {png_path} and {pdf_path}")


def create_container_diagram():
    fig, ax = plt.subplots(figsize=(15, 9), dpi=300)
    fig.patch.set_facecolor(COLOR_BG)
    ax.set_facecolor(COLOR_BG)
    ax.set_xlim(0, 1.5)
    ax.set_ylim(0, 1.0)
    ax.axis("off")

    # Header
    ax.text(0.75, 0.96, "Instakill — Container Diagram (C4 Level 2)",
            ha="center", va="center", fontsize=15, fontweight="bold", color=COLOR_TEXT_DARK)
    ax.text(0.75, 0.92, "Decomposition of the Instakill system into deployable runtime units and technologies",
            ha="center", va="center", fontsize=9.5, color="#64748B")

    # System boundary box
    sys_boundary = patches.FancyBboxPatch(
        (0.40, 0.05), 0.76, 0.83,
        boxstyle="round,pad=0.02,rounding_size=0.05",
        facecolor="#F1F5F9", edgecolor="#94A3B8", linewidth=1.5, linestyle="--"
    )
    ax.add_patch(sys_boundary)
    ax.text(0.78, 0.85, "Instakill System Boundary", ha="center", va="center",
            fontsize=10, fontweight="bold", color="#475569")

    # Person
    draw_box(ax, 0.06, 0.48, 0.24, 0.22,
             "User", "Person",
             "Interacts via browser on desktop\nor mobile devices.",
             COLOR_PERSON)

    # Container 1: Frontend SPA
    draw_box(ax, 0.44, 0.56, 0.30, 0.24,
             "Frontend SPA", "Container: Vue 3, Vite, Axios",
             "Single-page application delivering an\nInstagram-like reactive feed, composer,\nlikes toggle, and JWT cookie auth.\n(Port :5173)",
             COLOR_CONTAINER)

    # Container 2: Thymeleaf SSR
    draw_box(ax, 0.44, 0.24, 0.30, 0.24,
             "Server-Side MVC UI", "Container: Thymeleaf, Spring MVC",
             "Server-rendered HTML templates for\nfeed viewing and authentication fallback.\n(Port :8080/feed, /auth/login)",
             COLOR_CONTAINER)

    # Container 3: Backend API Monolith
    draw_box(ax, 0.82, 0.38, 0.31, 0.34,
             "Backend Application", "Container: Spring Boot 3.3.2, Java 21",
             "Exposes REST endpoints (/api/**) & MVC views.\nContains business domains: User, Post,\nFeed (Strategy), Interaction (Event Publisher).\nSecured by Spring Security 6 & JWT cookies.",
             COLOR_CONTAINER)

    # Container 4: SQLite Database
    draw_box(ax, 1.20, 0.38, 0.24, 0.30,
             "Database", "Container: SQLite 3",
             "Single-file relational database (instakill.db)\nstoring accounts, posts, likes, comments.\nAccessed through SQLite-JDBC & Hibernate.",
             COLOR_DATABASE, is_db=True)

    # External
    draw_box(ax, 0.06, 0.16, 0.24, 0.20,
             "External Image CDN", "External System",
             "Hosts remote media linked\ninside post imageUrl attributes.",
             COLOR_EXTERNAL)

    # Arrows
    draw_arrow(ax, 0.30, 0.62, 0.44, 0.66, "Interacts with SPA\n[HTTPS / Browser]")
    draw_arrow(ax, 0.30, 0.54, 0.44, 0.38, "Visits SSR pages\n[HTTP :8080 / HTML]")
    draw_arrow(ax, 0.74, 0.66, 0.82, 0.58, "REST API calls with JWT cookie\n[JSON / HTTP Proxy :8080]")
    draw_arrow(ax, 0.74, 0.38, 0.82, 0.44, "Internal MVC routing\n[In-process Spring Dispatcher]")
    draw_arrow(ax, 1.13, 0.52, 1.20, 0.52, "Queries & Transactions\n[JDBC / SQL / HikariCP]")
    draw_arrow(ax, 0.18, 0.48, 0.18, 0.36, "Loads images directly in browser\n[HTTPS]")

    plt.tight_layout()
    png_path = os.path.join(OUT_DIR, "c4_container.png")
    pdf_path = os.path.join(OUT_DIR, "c4_container.pdf")
    plt.savefig(png_path, dpi=300, bbox_inches="tight")
    plt.savefig(pdf_path, bbox_inches="tight")
    plt.close()
    print(f"Container diagram saved to {png_path} and {pdf_path}")


def create_component_diagram():
    fig, ax = plt.subplots(figsize=(17, 10.5), dpi=300)
    fig.patch.set_facecolor(COLOR_BG)
    ax.set_facecolor(COLOR_BG)
    ax.set_xlim(0, 1.7)
    ax.set_ylim(0, 1.05)
    ax.axis("off")

    # Header
    ax.text(0.85, 1.01, "Instakill — Backend Component Diagram (C4 Level 3)",
            ha="center", va="center", fontsize=15, fontweight="bold", color=COLOR_TEXT_DARK)
    ax.text(0.85, 0.97, "Detailed internal structure of the Spring Boot 3 Backend Container",
            ha="center", va="center", fontsize=9.5, color="#64748B")

    # Backend Container boundary
    backend_boundary = patches.FancyBboxPatch(
        (0.24, 0.04), 1.18, 0.90,
        boxstyle="round,pad=0.02,rounding_size=0.04",
        facecolor="#F8FAFC", edgecolor="#3B82F6", linewidth=1.6, linestyle="--"
    )
    ax.add_patch(backend_boundary)
    ax.text(0.83, 0.92, "Backend Container (Spring Boot Monolith)", ha="center", va="center",
            fontsize=11, fontweight="bold", color="#1E3A8A")

    # External Client callers
    draw_box(ax, 0.02, 0.62, 0.18, 0.18,
             "Vue 3 SPA", "Client",
             "Makes asynchronous REST calls\nwith HttpOnly cookie.",
             COLOR_CONTAINER)
    draw_box(ax, 0.02, 0.32, 0.18, 0.18,
             "Web Browser", "Client",
             "Direct browser navigation to\nHTML / MVC endpoints.",
             COLOR_CONTAINER)

    # 1. Controllers (Web / API layer)
    draw_box(ax, 0.28, 0.65, 0.26, 0.21,
             "REST Controllers", "Spring REST Controller",
             "AuthRestController, PostRestController,\nFeedRestController, InteractionRestController.\nExposes JSON API endpoints.",
             COLOR_COMPONENT)

    draw_box(ax, 0.28, 0.38, 0.26, 0.19,
             "Web MVC Controller", "Spring Web MVC",
             "FeedWebController:\nRenders Thymeleaf templates\nfor /feed, /auth/login, etc.",
             COLOR_COMPONENT)

    # 2. Security Component
    draw_box(ax, 0.28, 0.09, 0.26, 0.21,
             "Security & JWT Filter", "Spring Security",
             "JwtAuthenticationFilter & JwtTokenService:\nValidates ACCESS_TOKEN cookie,\npopulates SecurityContext.",
             "#475569")

    # 3. Domain Services Layer
    draw_box(ax, 0.60, 0.70, 0.24, 0.17,
             "PostService", "Spring Service",
             "Creates, validates, and fetches\nindividual user posts.",
             COLOR_COMPONENT)

    draw_box(ax, 0.60, 0.49, 0.24, 0.18,
             "FeedService", "Spring Service",
             "Selects strategy via Map<FeedSort, Strategy>\nto assemble user feeds without switch cases.",
             COLOR_COMPONENT)

    draw_box(ax, 0.60, 0.28, 0.24, 0.18,
             "InteractionService", "Spring Service",
             "Handles like toggling, comment creation,\nupdates aggregates, publishes domain events.",
             COLOR_COMPONENT)

    draw_box(ax, 0.60, 0.08, 0.24, 0.17,
             "UserService", "Spring Service",
             "Handles user registration, authentication,\nand password hashing (BCrypt).",
             COLOR_COMPONENT)

    # 4. Strategy & Event Pattern Components
    draw_box(ax, 0.88, 0.52, 0.24, 0.18,
             "Feed Strategies", "Strategy Pattern (@Component)",
             "• NewestFeedRankingStrategy\n• TrendingFeedRankingStrategy\nImplements FeedRankingStrategy.",
             "#059669")

    draw_box(ax, 0.88, 0.28, 0.24, 0.18,
             "Event Publisher", "Null Object Pattern (@Component)",
             "• DomainEventPublisher (Interface)\n• NoOpDomainEventPublisher\nPublishes LikeCreatedEvent safely.",
             "#D97706")

    # 5. Repositories / Data Access
    draw_box(ax, 1.16, 0.32, 0.23, 0.42,
             "JPA Repositories", "Spring Data JPA",
             "• PostRepository\n• UserRepository\n• LikeRepository\n• CommentRepository\nHibernate SQLite Dialect.",
             COLOR_DATABASE)

    # External Database
    draw_box(ax, 1.45, 0.38, 0.22, 0.30,
             "SQLite Database", "instakill.db",
             "Single-file relational database\nstoring tables: users, posts,\nlikes, comments.",
             COLOR_DATABASE, is_db=True)

    # Relationship Arrows
    draw_arrow(ax, 0.20, 0.72, 0.28, 0.74, "JSON / HTTP")
    draw_arrow(ax, 0.20, 0.42, 0.28, 0.46, "HTML / HTTP")
    draw_arrow(ax, 0.41, 0.65, 0.41, 0.30, "Secured by JWT")

    draw_arrow(ax, 0.54, 0.76, 0.60, 0.76, "Invokes")
    draw_arrow(ax, 0.54, 0.68, 0.60, 0.58, "Invokes")
    draw_arrow(ax, 0.54, 0.62, 0.60, 0.38, "Invokes")
    draw_arrow(ax, 0.54, 0.45, 0.60, 0.56, "Invokes")

    draw_arrow(ax, 0.72, 0.58, 0.88, 0.60, "Delegates to strategy")
    draw_arrow(ax, 0.72, 0.36, 0.88, 0.36, "Emits LikeCreatedEvent")

    draw_arrow(ax, 0.84, 0.76, 1.16, 0.64, "Queries")
    draw_arrow(ax, 0.84, 0.34, 1.16, 0.44, "Queries")
    draw_arrow(ax, 0.84, 0.16, 1.16, 0.36, "Queries")
    draw_arrow(ax, 1.00, 0.52, 1.16, 0.56, "findLatest()")

    draw_arrow(ax, 1.39, 0.52, 1.45, 0.52, "JDBC / SQL")

    plt.tight_layout()
    png_path = os.path.join(OUT_DIR, "c4_component.png")
    pdf_path = os.path.join(OUT_DIR, "c4_component.pdf")
    plt.savefig(png_path, dpi=300, bbox_inches="tight")
    plt.savefig(pdf_path, bbox_inches="tight")
    plt.close()
    print(f"Component diagram saved to {png_path} and {pdf_path}")


if __name__ == "__main__":
    create_context_diagram()
    create_container_diagram()
    create_component_diagram()
    print("All C4 diagrams generated successfully.")

