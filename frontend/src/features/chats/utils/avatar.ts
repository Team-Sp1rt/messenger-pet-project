export const avatarGradients = [
    'linear-gradient(135deg, #ff9a56, #ff6b6b)',
    'linear-gradient(135deg, #6a82fb, #fc5c7d)',
    'linear-gradient(135deg, #43cea2, #185a9d)',
    'linear-gradient(135deg, #f7971e, #ffd200)',
    'linear-gradient(135deg, #8e2de2, #4a00e0)',
]

export function getAvatarGradient(id: string) {
    return avatarGradients[id.charCodeAt(0) % avatarGradients.length]
}

export function getInitial(name: string) {
    return name.trim().charAt(0).toUpperCase()
}