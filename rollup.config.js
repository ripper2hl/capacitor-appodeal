import summary from 'rollup-plugin-summary';
import { terser } from 'rollup-plugin-terser';
import resolve from '@rollup/plugin-node-resolve';
import commonjs from '@rollup/plugin-commonjs';

export default {
    input: 'dist/esm/index.js',
    output: [
        {
            file: 'dist/plugin.js',
            format: 'iife',
            name: 'capacitorAppodeal',
            globals: {
                '@capacitor/core': 'capacitorExports',
            },
            sourcemap: true,
            inlineDynamicImports: true,
        },
        {
            file: 'dist/plugin.cjs.js',
            format: 'cjs',
            sourcemap: true,
            inlineDynamicImports: true,
        },
    ],
    external: ['@capacitor/core'],
    plugins: [
        resolve(),
        commonjs(),
        terser(),
        summary(),
    ],
};
