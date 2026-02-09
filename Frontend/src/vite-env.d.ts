/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APIUSER: string;
  readonly VITE_APIORDER: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
