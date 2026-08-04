export default {
  "*.{ts,tsx,js,jsx,json}": "biome check --write",
  "*.{ts,tsx}": () => "tsc --noEmit",
};
