# Operator Images

This directory contains operator character images for the Shadownet Nexus platform.

## Image Requirements

- **Format**: PNG, JPG, or WebP
- **Dimensions**: 1025 × 1600 pixels (portrait aspect ratio)
- **File Naming**: `{operator-id}.png` or `{operator-id}.jpg`
- **Size**: Keep under 500KB per image for optimal performance

## Operator IDs

Map your images to operator IDs. Update the operator data with correct image paths:

```
/src/assets/images/
├── kai.jpg                    # SPECTER (Kai)
├── echo.jpg                   # ECHO (Echo)
├── vex.jpg                    # VEX (Vex)
├── cipher.jpg                 # CIPHER (Cipher)
├── nova.jpg                   # NOVA (Nova)
├── sage.jpg                   # SAGE (Sage)
├── razor.jpg                  # RAZOR (Razor)
├── rogue.jpg                  # ROGUE (Rogue)
└── phantom.jpg                # PHANTOM (Phantom)
```

## Adding Images

1. Place your 1025×1600 portrait images in this directory
2. Match the filename to the operator ID
3. Update `src/data/gameData.ts` with the correct image path:

```typescript
portraitUrl: '/images/kai.jpg',
fullImageUrl: '/images/kai.jpg',
```

## Fallback

If an image is missing, a placeholder color background will be displayed with the operator's codename.

## Performance Tips

- Use lossy JPG for photos (~50-100KB)
- Use PNG for images with transparency
- Consider WebP for newer browsers (fallback to JPG)
- Optimize using tools like ImageOptim or TinyPNG
