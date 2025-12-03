--
-- PostgreSQL database dump
--

\restrict bDKwC1seE8kJzRU7O3fdQvIY942pQvE4vthdb8SyMAmE4dt87ETBOiZjziKX7sD

-- Dumped from database version 15.15 (Debian 15.15-1.pgdg13+1)
-- Dumped by pg_dump version 15.15 (Debian 15.15-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: activity_feed; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.activity_feed (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    project_id uuid,
    issue_id uuid,
    actor_id uuid NOT NULL,
    action character varying(50) NOT NULL,
    payload jsonb,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.activity_feed OWNER TO taskapp;

--
-- Name: attachments; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.attachments (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    issue_id uuid NOT NULL,
    original_filename character varying(255) NOT NULL,
    file_size bigint NOT NULL,
    mime_type character varying(100),
    uploaded_by uuid NOT NULL,
    filename character varying(255) NOT NULL,
    storage_path character varying(1000) NOT NULL,
    storage_type character varying(20) DEFAULT 'local'::character varying NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_storage_type CHECK (((storage_type)::text = ANY ((ARRAY['local'::character varying, 's3'::character varying, 'azure'::character varying])::text[])))
);


ALTER TABLE public.attachments OWNER TO taskapp;

--
-- Name: comments; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.comments (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    issue_id uuid NOT NULL,
    author_id uuid NOT NULL,
    content text NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.comments OWNER TO taskapp;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO taskapp;

--
-- Name: issue_history; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.issue_history (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    issue_id uuid NOT NULL,
    changed_by uuid NOT NULL,
    field character varying(100) NOT NULL,
    old_value text,
    new_value text,
    changed_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.issue_history OWNER TO taskapp;

--
-- Name: issue_labels; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.issue_labels (
    issue_id uuid NOT NULL,
    label_id uuid NOT NULL,
    added_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.issue_labels OWNER TO taskapp;

--
-- Name: issues; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.issues (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    project_id uuid NOT NULL,
    key character varying(50) NOT NULL,
    due_date date,
    sequential_number integer NOT NULL,
    title character varying(500) NOT NULL,
    description text,
    type character varying(20) DEFAULT 'task'::character varying NOT NULL,
    priority character varying(20) DEFAULT 'medium'::character varying NOT NULL,
    status uuid NOT NULL,
    reporter_id uuid NOT NULL,
    assignee_id uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    deleted_by uuid,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    resolved_at timestamp with time zone,
    closed_at timestamp with time zone,
    deleted_at timestamp with time zone,
    CONSTRAINT chk_issue_priority CHECK (((priority)::text = ANY ((ARRAY['low'::character varying, 'medium'::character varying, 'high'::character varying])::text[]))),
    CONSTRAINT chk_issue_type CHECK (((type)::text = ANY ((ARRAY['task'::character varying, 'bug'::character varying])::text[])))
);


ALTER TABLE public.issues OWNER TO taskapp;

--
-- Name: labels; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.labels (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(50) NOT NULL,
    color character varying(7) NOT NULL
);


ALTER TABLE public.labels OWNER TO taskapp;

--
-- Name: permissions; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.permissions (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying(100) NOT NULL,
    description text
);


ALTER TABLE public.permissions OWNER TO taskapp;

--
-- Name: project_issue_counters; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.project_issue_counters (
    project_id uuid NOT NULL,
    last_number integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.project_issue_counters OWNER TO taskapp;

--
-- Name: project_members; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.project_members (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    project_id uuid NOT NULL,
    user_id uuid NOT NULL,
    role character varying(20) DEFAULT 'member'::character varying NOT NULL,
    joined_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_project_role CHECK (((role)::text = ANY ((ARRAY['admin'::character varying, 'member'::character varying, 'viewer'::character varying])::text[])))
);


ALTER TABLE public.project_members OWNER TO taskapp;

--
-- Name: projects; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.projects (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    key character varying(10) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    visibility character varying(20) DEFAULT 'private'::character varying NOT NULL,
    created_by uuid NOT NULL,
    is_archived boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    archived_at timestamp with time zone,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_visibility CHECK (((visibility)::text = ANY ((ARRAY['public'::character varying, 'private'::character varying, 'internal'::character varying])::text[])))
);


ALTER TABLE public.projects OWNER TO taskapp;

--
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.refresh_tokens (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    user_id uuid NOT NULL,
    token character varying(500) NOT NULL,
    expires_at timestamp with time zone NOT NULL
);


ALTER TABLE public.refresh_tokens OWNER TO taskapp;

--
-- Name: role_permissions; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.role_permissions (
    role_id uuid NOT NULL,
    permission_id uuid NOT NULL
);


ALTER TABLE public.role_permissions OWNER TO taskapp;

--
-- Name: roles; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.roles (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(50) NOT NULL,
    description text
);


ALTER TABLE public.roles OWNER TO taskapp;

--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.user_roles (
    user_id uuid NOT NULL,
    role_id uuid NOT NULL
);


ALTER TABLE public.user_roles OWNER TO taskapp;

--
-- Name: users; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.users (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    email character varying(255) NOT NULL,
    avatar_url character varying(1000),
    name character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_login_at timestamp with time zone
);


ALTER TABLE public.users OWNER TO taskapp;

--
-- Name: workflow_statuses; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.workflow_statuses (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    workflow_id uuid NOT NULL,
    code character varying(50) NOT NULL,
    color character varying(20),
    name character varying(100) NOT NULL,
    "position" integer NOT NULL
);


ALTER TABLE public.workflow_statuses OWNER TO taskapp;

--
-- Name: workflow_transitions; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.workflow_transitions (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    workflow_id uuid NOT NULL,
    from_code character varying(50) NOT NULL,
    to_code character varying(50) NOT NULL,
    is_automatic boolean DEFAULT false NOT NULL
);


ALTER TABLE public.workflow_transitions OWNER TO taskapp;

--
-- Name: workflows; Type: TABLE; Schema: public; Owner: taskapp
--

CREATE TABLE public.workflows (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    project_id uuid NOT NULL,
    name character varying(100) NOT NULL
);


ALTER TABLE public.workflows OWNER TO taskapp;

--
-- Data for Name: activity_feed; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.activity_feed (id, project_id, issue_id, actor_id, action, payload, created_at) FROM stdin;
\.


--
-- Data for Name: attachments; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.attachments (id, issue_id, original_filename, file_size, mime_type, uploaded_by, filename, storage_path, storage_type, is_deleted, created_at) FROM stdin;
\.


--
-- Data for Name: comments; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.comments (id, issue_id, author_id, content, is_deleted, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	create users and roles	SQL	V1__create_users_and_roles.sql	-188422119	taskapp	2025-11-19 14:35:50.508682	429	t
2	2	create projects	SQL	V2__create_projects.sql	154293497	taskapp	2025-11-19 14:35:51.059336	139	t
3	3	create workflows	SQL	V3__create_workflows.sql	-1011055248	taskapp	2025-11-19 14:35:51.260523	96	t
4	4	create issues	SQL	V4__create_issues.sql	-1404378817	taskapp	2025-11-19 14:35:51.416143	201	t
5	5	create comments and attachments	SQL	V5__create_comments_and_attachments.sql	-331562458	taskapp	2025-11-19 14:35:51.667269	112	t
6	6	create labels	SQL	V6__create_labels.sql	1506214717	taskapp	2025-11-19 14:35:51.831982	83	t
7	7	create activity feed	SQL	V7__create_activity_feed.sql	705155774	taskapp	2025-11-19 14:35:51.958575	81	t
\.


--
-- Data for Name: issue_history; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.issue_history (id, issue_id, changed_by, field, old_value, new_value, changed_at) FROM stdin;
\.


--
-- Data for Name: issue_labels; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.issue_labels (issue_id, label_id, added_at) FROM stdin;
\.


--
-- Data for Name: issues; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.issues (id, project_id, key, due_date, sequential_number, title, description, type, priority, status, reporter_id, assignee_id, is_deleted, deleted_by, created_at, updated_at, resolved_at, closed_at, deleted_at) FROM stdin;
\.


--
-- Data for Name: labels; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.labels (id, name, color) FROM stdin;
9752d946-7912-40c3-b702-ae7b448f8e4d	bug	#d73a4a
1bcbc404-8b1c-4fae-b074-72a74618ac23	enhancement	#a2eeef
c74fe48b-dfaa-4b5e-b375-47f008c0efe8	documentation	#0075ca
7d0813cf-3f00-4b80-9e59-4aaa4a51bd41	urgent	#b60205
f5f730e5-4dfc-4b1a-9078-e3926cb89a2b	help wanted	#008672
\.


--
-- Data for Name: permissions; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.permissions (id, code, description) FROM stdin;
1900bfff-2030-4a53-82e6-5bca0951eef3	USER_READ	Can view users
ceedeb9d-cf53-4b7a-be3a-f749bffe6d1d	USER_WRITE	Can create and update users
ba4d9e16-4ce2-41e9-aaff-ebf998148693	USER_DELETE	Can delete users
3d327d9d-42fb-4ec1-af6f-1550f24ecd7e	PROJECT_READ	Can view projects
bbf5af5f-bfbf-4cad-828f-99ffba8bd147	PROJECT_WRITE	Can create and update projects
09e274f4-c645-4384-8a53-aa0c3d4277e3	PROJECT_DELETE	Can delete projects
73551aa5-e1dd-446a-b667-66d0057e04ec	ISSUE_READ	Can view issues
9917f25e-6329-4d32-97e5-adabd563c33d	ISSUE_WRITE	Can create and update issues
1f186a7a-6343-4fbb-b81f-192e3e4fdabf	ISSUE_DELETE	Can delete issues
a6711d4b-9ba5-4226-a4c1-8faa694b502c	USER_CREATE	Create new users
5a72d3d1-5feb-417d-b474-81f4b181d284	USER_UPDATE	Update user information
760d480a-115a-4753-bbc9-7cc20247ab3b	ROLE_CREATE	Create new roles
1b321e0c-1218-4298-9c7f-01fbd9bf1c5f	ROLE_READ	View role details
c96819ca-0ad3-47bb-92ef-6d05bd03c1a9	ROLE_UPDATE	Update role information
78b3266f-de70-4bd9-941b-cdb0e27fa14d	ROLE_DELETE	Delete roles
af5a2009-0413-482f-8ef2-263e7da8b05a	ROLE_ASSIGN	Assign roles to users
7d3b9a7a-7f50-416f-bbd6-067d8d88edb4	PERMISSION_CREATE	Create new permissions
f48d73e2-0e8f-4a92-9e8e-6c9633ee4aa9	PERMISSION_READ	View permission details
4c6f8de8-2ac8-4bc1-9d40-568d5c0deab9	PERMISSION_UPDATE	Update permission information
0cef86a8-c742-4c98-903d-8ed27a322c6f	PERMISSION_DELETE	Delete permissions
96e75d1c-b9f4-42c7-ba74-3c27839fef65	PERMISSION_ASSIGN	Assign permissions to roles
b7d781ff-809d-4d93-9e05-377c46cbd623	TASK_CREATE	Create new tasks
2423ebf2-c290-435e-a6f8-c79d7db1f97c	TASK_READ	View task details
46b24dc5-09da-4bfc-a9a8-4f118411c240	TASK_UPDATE	Update task information
8c2ae8fc-f9ac-446e-b6c2-d99389a816a0	TASK_DELETE	Delete tasks
e008e3a4-e37c-4c9b-ad13-d36b632e1f31	TASK_ASSIGN	Assign tasks to users
e9745e78-ff22-43cb-b0ff-6833a34faf7f	PROJECT_CREATE	Create new projects
f50463a1-e2b1-423b-acd1-fc4d1e287e40	PROJECT_UPDATE	Update project information
f09dea7e-8a80-4e32-a55f-69754a8c5e16	PROJECT_MANAGE	Full project management
4017ea66-625d-4bc0-99fd-f84fe1b3bbe3	SYSTEM_ADMIN	Full system administration
d6e9a469-9103-4f99-a97b-3de7d60d853e	AUDIT_READ	View audit logs
40460989-b3a9-4298-a70e-cf72c86649aa	SETTINGS_MANAGE	Manage system settings
\.


--
-- Data for Name: project_issue_counters; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.project_issue_counters (project_id, last_number) FROM stdin;
\.


--
-- Data for Name: project_members; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.project_members (id, project_id, user_id, role, joined_at) FROM stdin;
\.


--
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.projects (id, key, name, description, visibility, created_by, is_archived, created_at, archived_at, updated_at) FROM stdin;
\.


--
-- Data for Name: refresh_tokens; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.refresh_tokens (id, user_id, token, expires_at) FROM stdin;
\.


--
-- Data for Name: role_permissions; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.role_permissions (role_id, permission_id) FROM stdin;
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.roles (id, name, description) FROM stdin;
9e744ab3-eb22-4a49-be50-5a343fc48275	ROLE_ADMIN	System administrator with full access
e54a5546-b078-4cf4-894f-fecf0668a15a	ROLE_USER	Regular user with standard access
fa74fa8e-5d1c-495a-b64c-2554450ece75	ROLE_MANAGER	Manager role with elevated privileges
827832a6-a7f2-445f-9888-a97f58c5d98c	ROLE_GUEST	Guest role with limited access
\.


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.user_roles (user_id, role_id) FROM stdin;
41953fa3-1713-48bd-8e05-65599c6ee853	e54a5546-b078-4cf4-894f-fecf0668a15a
77abc388-6813-4c2c-abf2-19bb0e7cc2a2	e54a5546-b078-4cf4-894f-fecf0668a15a
7ff7367f-f47d-49a6-946e-d1f6d78c14f7	e54a5546-b078-4cf4-894f-fecf0668a15a
25740202-1f3e-468b-a4f5-cb838553995c	e54a5546-b078-4cf4-894f-fecf0668a15a
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.users (id, email, avatar_url, name, password_hash, enabled, created_at, updated_at, last_login_at) FROM stdin;
41953fa3-1713-48bd-8e05-65599c6ee853	patelrajkumar362002@gmail.com	https://images.pexels.com/photos/29531015/pexels-photo-29531015.jpeg	rajkumar patel	$2a$10$Y9DCe9QkxlENH.LUptPWaePTTnbC8KVzfanbLEUN5q.kAz6AjdYty	t	2025-11-23 10:47:40.009368+00	2025-11-23 10:47:40.009368+00	\N
77abc388-6813-4c2c-abf2-19bb0e7cc2a2	rajkumarp.patel@wishtreetech.com	https://images.pexels.com/photos/29531015/pexels-photo-29531015.jpeg	rajkumar patel	$2a$10$1s71hd8T2OT3JUirS6VKPenLP0q5F1kvX9r9riOwOTiVfPm457cb6	t	2025-11-25 18:11:58.264654+00	2025-11-25 18:11:58.264654+00	\N
7ff7367f-f47d-49a6-946e-d1f6d78c14f7	taskapp16@gmail.com	https://images.pexels.com/photos/29531015/pexels-photo-29531015.jpeg	rajkumar patel	$2a$10$/A/gESkmhqduWydAOQ4R1e2/R2UFePoAZcgCYynXCpwC2wdC8ow2K	f	2025-11-25 18:21:58.409008+00	2025-11-25 18:21:58.409008+00	\N
25740202-1f3e-468b-a4f5-cb838553995c	patelrajkumar362001@gmail.com	https://images.pexels.com/photos/29531015/pexels-photo-29531015.jpeg	rajkumar patel	$2a$10$qxjn/fbh0wSOv1RdxcBPw.6xXVryhc4G8wW09kdamrTOneMVYsFsW	t	2025-11-25 19:08:07.617658+00	2025-11-25 19:08:07.617658+00	\N
\.


--
-- Data for Name: workflow_statuses; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.workflow_statuses (id, workflow_id, code, color, name, "position") FROM stdin;
\.


--
-- Data for Name: workflow_transitions; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.workflow_transitions (id, workflow_id, from_code, to_code, is_automatic) FROM stdin;
\.


--
-- Data for Name: workflows; Type: TABLE DATA; Schema: public; Owner: taskapp
--

COPY public.workflows (id, project_id, name) FROM stdin;
\.


--
-- Name: activity_feed activity_feed_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.activity_feed
    ADD CONSTRAINT activity_feed_pkey PRIMARY KEY (id);


--
-- Name: attachments attachments_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.attachments
    ADD CONSTRAINT attachments_pkey PRIMARY KEY (id);


--
-- Name: comments comments_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: issue_history issue_history_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issue_history
    ADD CONSTRAINT issue_history_pkey PRIMARY KEY (id);


--
-- Name: issue_labels issue_labels_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issue_labels
    ADD CONSTRAINT issue_labels_pkey PRIMARY KEY (issue_id, label_id);


--
-- Name: issues issues_key_key; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issues
    ADD CONSTRAINT issues_key_key UNIQUE (key);


--
-- Name: issues issues_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issues
    ADD CONSTRAINT issues_pkey PRIMARY KEY (id);


--
-- Name: issues issues_project_id_sequential_number_key; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issues
    ADD CONSTRAINT issues_project_id_sequential_number_key UNIQUE (project_id, sequential_number);


--
-- Name: labels labels_name_key; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.labels
    ADD CONSTRAINT labels_name_key UNIQUE (name);


--
-- Name: labels labels_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.labels
    ADD CONSTRAINT labels_pkey PRIMARY KEY (id);


--
-- Name: permissions permissions_code_key; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_code_key UNIQUE (code);


--
-- Name: permissions permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (id);


--
-- Name: project_issue_counters project_issue_counters_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.project_issue_counters
    ADD CONSTRAINT project_issue_counters_pkey PRIMARY KEY (project_id);


--
-- Name: project_members project_members_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.project_members
    ADD CONSTRAINT project_members_pkey PRIMARY KEY (id);


--
-- Name: project_members project_members_project_id_user_id_key; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.project_members
    ADD CONSTRAINT project_members_project_id_user_id_key UNIQUE (project_id, user_id);


--
-- Name: projects projects_key_key; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT projects_key_key UNIQUE (key);


--
-- Name: projects projects_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT projects_pkey PRIMARY KEY (id);


--
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: refresh_tokens refresh_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_token_key UNIQUE (token);


--
-- Name: role_permissions role_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_pkey PRIMARY KEY (role_id, permission_id);


--
-- Name: roles roles_name_key; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_name_key UNIQUE (name);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: workflow_statuses workflow_statuses_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.workflow_statuses
    ADD CONSTRAINT workflow_statuses_pkey PRIMARY KEY (id);


--
-- Name: workflow_statuses workflow_statuses_workflow_id_code_key; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.workflow_statuses
    ADD CONSTRAINT workflow_statuses_workflow_id_code_key UNIQUE (workflow_id, code);


--
-- Name: workflow_transitions workflow_transitions_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.workflow_transitions
    ADD CONSTRAINT workflow_transitions_pkey PRIMARY KEY (id);


--
-- Name: workflow_transitions workflow_transitions_workflow_id_from_code_to_code_key; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.workflow_transitions
    ADD CONSTRAINT workflow_transitions_workflow_id_from_code_to_code_key UNIQUE (workflow_id, from_code, to_code);


--
-- Name: workflows workflows_pkey; Type: CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.workflows
    ADD CONSTRAINT workflows_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_activity_feed_actor_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_activity_feed_actor_id ON public.activity_feed USING btree (actor_id);


--
-- Name: idx_activity_feed_created_at; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_activity_feed_created_at ON public.activity_feed USING btree (created_at DESC);


--
-- Name: idx_activity_feed_issue_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_activity_feed_issue_id ON public.activity_feed USING btree (issue_id, created_at DESC);


--
-- Name: idx_activity_feed_project_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_activity_feed_project_id ON public.activity_feed USING btree (project_id, created_at DESC);


--
-- Name: idx_attachments_issue_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_attachments_issue_id ON public.attachments USING btree (issue_id);


--
-- Name: idx_attachments_uploaded_by; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_attachments_uploaded_by ON public.attachments USING btree (uploaded_by);


--
-- Name: idx_comments_author_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_comments_author_id ON public.comments USING btree (author_id);


--
-- Name: idx_comments_created_at; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_comments_created_at ON public.comments USING btree (created_at DESC);


--
-- Name: idx_comments_issue_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_comments_issue_id ON public.comments USING btree (issue_id);


--
-- Name: idx_issue_history_changed_at; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_issue_history_changed_at ON public.issue_history USING btree (changed_at DESC);


--
-- Name: idx_issue_history_issue_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_issue_history_issue_id ON public.issue_history USING btree (issue_id);


--
-- Name: idx_issue_labels_issue_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_issue_labels_issue_id ON public.issue_labels USING btree (issue_id);


--
-- Name: idx_issue_labels_label_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_issue_labels_label_id ON public.issue_labels USING btree (label_id);


--
-- Name: idx_issues_assignee_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_issues_assignee_id ON public.issues USING btree (assignee_id);


--
-- Name: idx_issues_created_at; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_issues_created_at ON public.issues USING btree (created_at DESC);


--
-- Name: idx_issues_deleted; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_issues_deleted ON public.issues USING btree (is_deleted) WHERE (is_deleted = false);


--
-- Name: idx_issues_key; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE UNIQUE INDEX idx_issues_key ON public.issues USING btree (key);


--
-- Name: idx_issues_priority; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_issues_priority ON public.issues USING btree (priority);


--
-- Name: idx_issues_project_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_issues_project_id ON public.issues USING btree (project_id);


--
-- Name: idx_issues_reporter_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_issues_reporter_id ON public.issues USING btree (reporter_id);


--
-- Name: idx_issues_status; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_issues_status ON public.issues USING btree (status);


--
-- Name: idx_issues_type; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_issues_type ON public.issues USING btree (type);


--
-- Name: idx_labels_name; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_labels_name ON public.labels USING btree (name);


--
-- Name: idx_project_members_project_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_project_members_project_id ON public.project_members USING btree (project_id);


--
-- Name: idx_project_members_user_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_project_members_user_id ON public.project_members USING btree (user_id);


--
-- Name: idx_projects_archived; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_projects_archived ON public.projects USING btree (is_archived) WHERE (is_archived = false);


--
-- Name: idx_projects_created_by; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_projects_created_by ON public.projects USING btree (created_by);


--
-- Name: idx_projects_key; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_projects_key ON public.projects USING btree (key);


--
-- Name: idx_refresh_tokens_token; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_refresh_tokens_token ON public.refresh_tokens USING btree (token);


--
-- Name: idx_refresh_tokens_user_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_refresh_tokens_user_id ON public.refresh_tokens USING btree (user_id);


--
-- Name: idx_user_roles_role_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_user_roles_role_id ON public.user_roles USING btree (role_id);


--
-- Name: idx_user_roles_user_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_user_roles_user_id ON public.user_roles USING btree (user_id);


--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- Name: idx_users_enabled; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_users_enabled ON public.users USING btree (enabled) WHERE (enabled = true);


--
-- Name: idx_workflow_statuses_code; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_workflow_statuses_code ON public.workflow_statuses USING btree (workflow_id, code);


--
-- Name: idx_workflow_statuses_workflow_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_workflow_statuses_workflow_id ON public.workflow_statuses USING btree (workflow_id);


--
-- Name: idx_workflow_transitions_workflow_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_workflow_transitions_workflow_id ON public.workflow_transitions USING btree (workflow_id);


--
-- Name: idx_workflows_project_id; Type: INDEX; Schema: public; Owner: taskapp
--

CREATE INDEX idx_workflows_project_id ON public.workflows USING btree (project_id);


--
-- Name: activity_feed activity_feed_actor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.activity_feed
    ADD CONSTRAINT activity_feed_actor_id_fkey FOREIGN KEY (actor_id) REFERENCES public.users(id);


--
-- Name: activity_feed activity_feed_issue_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.activity_feed
    ADD CONSTRAINT activity_feed_issue_id_fkey FOREIGN KEY (issue_id) REFERENCES public.issues(id) ON DELETE CASCADE;


--
-- Name: activity_feed activity_feed_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.activity_feed
    ADD CONSTRAINT activity_feed_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id) ON DELETE CASCADE;


--
-- Name: attachments attachments_issue_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.attachments
    ADD CONSTRAINT attachments_issue_id_fkey FOREIGN KEY (issue_id) REFERENCES public.issues(id) ON DELETE CASCADE;


--
-- Name: attachments attachments_uploaded_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.attachments
    ADD CONSTRAINT attachments_uploaded_by_fkey FOREIGN KEY (uploaded_by) REFERENCES public.users(id);


--
-- Name: comments comments_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_author_id_fkey FOREIGN KEY (author_id) REFERENCES public.users(id);


--
-- Name: comments comments_issue_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_issue_id_fkey FOREIGN KEY (issue_id) REFERENCES public.issues(id) ON DELETE CASCADE;


--
-- Name: issue_history issue_history_changed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issue_history
    ADD CONSTRAINT issue_history_changed_by_fkey FOREIGN KEY (changed_by) REFERENCES public.users(id);


--
-- Name: issue_history issue_history_issue_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issue_history
    ADD CONSTRAINT issue_history_issue_id_fkey FOREIGN KEY (issue_id) REFERENCES public.issues(id) ON DELETE CASCADE;


--
-- Name: issue_labels issue_labels_issue_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issue_labels
    ADD CONSTRAINT issue_labels_issue_id_fkey FOREIGN KEY (issue_id) REFERENCES public.issues(id) ON DELETE CASCADE;


--
-- Name: issue_labels issue_labels_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issue_labels
    ADD CONSTRAINT issue_labels_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.labels(id) ON DELETE CASCADE;


--
-- Name: issues issues_assignee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issues
    ADD CONSTRAINT issues_assignee_id_fkey FOREIGN KEY (assignee_id) REFERENCES public.users(id);


--
-- Name: issues issues_deleted_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issues
    ADD CONSTRAINT issues_deleted_by_fkey FOREIGN KEY (deleted_by) REFERENCES public.users(id);


--
-- Name: issues issues_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issues
    ADD CONSTRAINT issues_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id) ON DELETE CASCADE;


--
-- Name: issues issues_reporter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issues
    ADD CONSTRAINT issues_reporter_id_fkey FOREIGN KEY (reporter_id) REFERENCES public.users(id);


--
-- Name: issues issues_status_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.issues
    ADD CONSTRAINT issues_status_fkey FOREIGN KEY (status) REFERENCES public.workflow_statuses(id);


--
-- Name: project_issue_counters project_issue_counters_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.project_issue_counters
    ADD CONSTRAINT project_issue_counters_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id) ON DELETE CASCADE;


--
-- Name: project_members project_members_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.project_members
    ADD CONSTRAINT project_members_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id) ON DELETE CASCADE;


--
-- Name: project_members project_members_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.project_members
    ADD CONSTRAINT project_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: projects projects_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT projects_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: refresh_tokens refresh_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: role_permissions role_permissions_permission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES public.permissions(id) ON DELETE CASCADE;


--
-- Name: role_permissions role_permissions_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- Name: user_roles user_roles_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- Name: user_roles user_roles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: workflow_statuses workflow_statuses_workflow_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.workflow_statuses
    ADD CONSTRAINT workflow_statuses_workflow_id_fkey FOREIGN KEY (workflow_id) REFERENCES public.workflows(id) ON DELETE CASCADE;


--
-- Name: workflow_transitions workflow_transitions_workflow_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.workflow_transitions
    ADD CONSTRAINT workflow_transitions_workflow_id_fkey FOREIGN KEY (workflow_id) REFERENCES public.workflows(id) ON DELETE CASCADE;


--
-- Name: workflows workflows_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: taskapp
--

ALTER TABLE ONLY public.workflows
    ADD CONSTRAINT workflows_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict bDKwC1seE8kJzRU7O3fdQvIY942pQvE4vthdb8SyMAmE4dt87ETBOiZjziKX7sD

