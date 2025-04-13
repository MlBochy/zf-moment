// ÔÚ utils/date.js ÖÐÌí¼Ó
export const formatTime = (timestamp) => {
    const date = new Date(timestamp)
    return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  const pad = (num) => num.toString().padStart(2, '0')