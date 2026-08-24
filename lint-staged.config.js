export default {
  "*.{ts,tsx,js,jsx,json}": "biome check --write --no-errors-on-unmatched",
  "*.{ts,tsx}": () => "tsc --noEmit",
};
