import { configureStore } from '@reduxjs/toolkit'
import { setupListeners } from '@reduxjs/toolkit/query'
import { puzzleApi } from './api/puzzleApi'
import { storyApi } from './api/storyApi'
import { operatorApi } from './api/operatorApi'
import { shadownetApi } from './api/shadownetApi'
import { teamApi } from './api/teamApi'
import { userApi } from './api/userApi'

export const store = configureStore({
  reducer: {
    [puzzleApi.reducerPath]: puzzleApi.reducer,
    [storyApi.reducerPath]: storyApi.reducer,
    [operatorApi.reducerPath]: operatorApi.reducer,
    [shadownetApi.reducerPath]: shadownetApi.reducer,
    [teamApi.reducerPath]: teamApi.reducer,
    [userApi.reducerPath]: userApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(
      puzzleApi.middleware,
      storyApi.middleware,
      operatorApi.middleware,
      shadownetApi.middleware,
      teamApi.middleware,
      userApi.middleware
    ),
})

setupListeners(store.dispatch)

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch

