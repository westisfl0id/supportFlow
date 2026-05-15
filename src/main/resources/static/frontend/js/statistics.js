import { get } from './api.js';

export async function loadOverviewStatistics() {
    return get('/statistics/overview');
}

export async function loadMyStatistics() {
    return get('/statistics/me');
}