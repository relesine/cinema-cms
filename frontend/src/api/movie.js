import request from '@/utils/request'

// ===== 用户端：电影 / 场次 / 座位 / 公告 =====
export const getHotMovies = () => request.get('/app/movie/hot')
export const getMovieList = (params) => request.get('/app/movie/list', { params })
export const getMovieDetail = (id) => request.get(`/app/movie/detail/${id}`)
export const getSchedulesByMovie = (movieId, date) =>
  request.get(`/app/schedule/movie/${movieId}`, { params: { date } })
export const getScheduleDetail = (id) => request.get(`/app/schedule/${id}`)
export const getSeatMap = (scheduleId) => request.get(`/app/schedule/seat/${scheduleId}`)
export const getNotices = () => request.get('/app/notice/list')
