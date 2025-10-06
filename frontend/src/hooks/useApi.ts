import { useState, useCallback } from 'react';
import axios, { AxiosResponse, AxiosError } from 'axios';

interface ApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

interface ApiOptions {
  onSuccess?: (data: any) => void;
  onError?: (error: string) => void;
  showSuccessMessage?: boolean;
}

export const useApi = <T = any>() => {
  const [state, setState] = useState<ApiState<T>>({
    data: null,
    loading: false,
    error: null,
  });

  const execute = useCallback(async (
    apiCall: () => Promise<AxiosResponse<T>>,
    options: ApiOptions = {}
  ) => {
    setState(prev => ({ ...prev, loading: true, error: null }));

    try {
      const response = await apiCall();
      setState({
        data: response.data,
        loading: false,
        error: null,
      });

      if (options.onSuccess) {
        options.onSuccess(response.data);
      }

      return response.data;
    } catch (error) {
      const errorMessage = getErrorMessage(error as AxiosError);
      setState(prev => ({
        ...prev,
        loading: false,
        error: errorMessage,
      }));

      if (options.onError) {
        options.onError(errorMessage);
      } else {
        console.error('API Error:', errorMessage);
      }

      throw error;
    }
  }, []);

  const reset = useCallback(() => {
    setState({
      data: null,
      loading: false,
      error: null,
    });
  }, []);

  return {
    ...state,
    execute,
    reset,
  };
};

// Helper function to extract meaningful error messages
const getErrorMessage = (error: AxiosError): string => {
  if (error.response) {
    // Server responded with error status
    const { status, data } = error.response;
    
    if (typeof data === 'object' && data !== null) {
      if ('message' in data) {
        return (data as any).message;
      }
      if ('error' in data) {
        return (data as any).error;
      }
    }
    
    switch (status) {
      case 400:
        return 'Invalid request. Please check your input and try again.';
      case 401:
        return 'You are not authorized. Please log in and try again.';
      case 403:
        return 'You do not have permission to perform this action.';
      case 404:
        return 'The requested resource was not found.';
      case 409:
        return 'A conflict occurred. The resource may already exist.';
      case 422:
        return 'The request data is invalid. Please check your input.';
      case 500:
        return 'A server error occurred. Please try again later.';
      case 503:
        return 'The service is temporarily unavailable. Please try again later.';
      default:
        return `An error occurred (${status}). Please try again.`;
    }
  } else if (error.request) {
    // Network error
    return 'Network error. Please check your internet connection and try again.';
  } else {
    // Other error
    return error.message || 'An unexpected error occurred. Please try again.';
  }
};

export default useApi;