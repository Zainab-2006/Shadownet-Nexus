import type { QueryClient, QueryKey } from '@tanstack/react-query';

export type MutationContext = { previous: unknown };

export const prepareMutationContext = async (
  queryClient: QueryClient,
  queryKey: QueryKey,
  label: string,
): Promise<MutationContext> => {
  try {
    await queryClient.cancelQueries({ queryKey });
    const previous = queryClient.getQueryData(queryKey);
    queryClient.setQueryData(queryKey, 'pending');
    return { previous };
  } catch (error) {
    console.error(`Failed to prepare ${label} mutation context:`, error);
    throw error;
  }
};
