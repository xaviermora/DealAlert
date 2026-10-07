interface AlertData {
    type: 'success' | 'error',
    text: string,
    actionText?: string,
    action?: () => void,
    duration?: number // ms
}