# Public Images Directory

This directory contains static image assets that are served directly by the web server.

## Structure

```
public/
├── images/
│   ├── operators/          # Operator character portraits
│   │   ├── kai.jpg
│   │   ├── echo.jpg
│   │   └── ...
│   └── environment/        # Background and environment images
```

## Adding Operator Images

1. Create an `operators/` subdirectory if it doesn't exist
2. Add your operator portrait images:
   - Format: PNG or JPG (1025×1600 pixels)
   - Filename: Use the operator ID (e.g., `kai.jpg`, `echo.png`)
   - Size: Keep under 500KB for performance

3. Update your operator data to reference these images:

```typescript
// In src/data/gameData.ts
portraitUrl: '/images/operators/kai.jpg',        // For cards
fullImageUrl: '/images/operators/kai.jpg',       // For modal
```

## Image Path Examples

- Card image: `/images/operators/kai.jpg`
- Modal image: `/images/operators/kai.jpg`
- 3D model: `/models/operators/kai.glb`

## Performance Optimization

```bash
# Optimize images before uploading
imageoptim images/operators/*.jpg
# or
cwebp images/operators/*.jpg -o images/operators/*.webp
```

## Fallback

If an image fails to load or is not found:
- The component will display a colored placeholder with operator initials
- This ensures the UI works even without images
- Check browser console for failed image load errors

## Development

During development, you can use placeholder images from these free sources:
- [Placeholder.com](https://placeholder.com)
- [UI Faces](https://uifaces.co)
- [Unsplash](https://unsplash.com)
- [Pexels](https://pexels.com)

Create 1025×1600 placeholders for testing:
```
https://placeholder.com/1025x1600?text=OPERATOR_NAME
```
