import {createContext, useContext, useState, useEffect, type ReactNode} from 'react';
import type {Theme} from '../types';

interface ThemeContextType {
    theme: Theme;
    toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

/**
 * Safely load theme from localStorage
 */
function getStoredTheme(): Theme {
    try {
        const saved = localStorage.getItem('theme');

        // Type guard: Validate if value is valid Theme
        if (saved === 'light' || saved === 'dark') {
            return saved;
        }

        // Return default if invalid value
        return 'dark';
    } catch (error) {
        // When localStorage access fails (Private Browsing mode, etc.)
        if (import.meta.env.DEV) {
            console.warn('localStorage access failed, using default theme:', error);
        }
        return 'dark';
    }
}

/**
 * Safely save theme to localStorage
 */
function setStoredTheme(theme: Theme): void {
    try {
        localStorage.setItem('theme', theme);
    } catch (error) {
        // When localStorage write fails (quota exceeded, no permission)
        if (import.meta.env.DEV) {
            console.warn('localStorage save failed:', error);
        }
        // App continues to work even on failure (stored in memory only)
    }
}

export const ThemeProvider = ({children}: { children: ReactNode }) => {
    // Initial value: Safely load from localStorage
    const [theme, setTheme] = useState<Theme>(getStoredTheme);

    useEffect(() => {
        // Update HTML class (Tailwind dark mode)
        if (theme === 'dark') {
            document.documentElement.classList.add('dark');
        } else {
            document.documentElement.classList.remove('dark');
        }

        // Safely save to localStorage
        setStoredTheme(theme);
    }, [theme]);

    const toggleTheme = () => {
        setTheme((prev) => (prev === 'light' ? 'dark' : 'light'));
    };

    return (
        <ThemeContext.Provider value={{theme, toggleTheme}}>
            {children}
        </ThemeContext.Provider>
    );
};

export const useTheme = () => {
    const context = useContext(ThemeContext);
    if (!context) {
        throw new Error('useTheme must be used within ThemeProvider');
    }
    return context;
};
